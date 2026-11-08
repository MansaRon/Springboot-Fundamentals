package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.EmailService;
import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.business.RefreshTokenService;
import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.user.*;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.exception.EmailException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.RefreshTokenRepository;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import co.za.ecommerce.utils.DateUtil;
import co.za.ecommerce.utils.GenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OTPService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    /**
     * How long an admin-issued temporary password remains valid for login.
     */
    @Value("${app.security.temp-password-ttl-hours:24}")
    private long tempPasswordTtlHours;

    @Override
    public User createUser(UserCreateDTO userCreateDTO) {
        log.info("============= Checking existing user ===============");
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        if (userRepository.existsByPhone(userCreateDTO.getPhone())) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Phone number already exists.");
        }

        log.info("============= Creating a user ===============");

        User user = User.builder()
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .name(userCreateDTO.getName())
                .email(userCreateDTO.getEmail())
                .phone(userCreateDTO.getPhone())
                .status(AccountStatus.AWAITING_CONFIRMATION)
                .password(passwordEncoder.encode(userCreateDTO.getPwd()))
                .build();

        user.addRoles("ROLE_USER");
        log.info("============= Saving user ===============");
        userRepository.save(user);

        log.info("============= Generate OTP ===============");
        otpService.generateOTP(user.getPhone());
        return user;
    }

    @Override
    public GetAllUsersDTO getAllUsers(int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Fetching users with pageNo: {}, pageSize: {}, sortBy: {}, sortDir: {}",
                pageNo, pageSize, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<User> users = userRepository.findAll(pageable);

        return buildGetAllUsersDTO(users);
    }

    private GetAllUsersDTO buildGetAllUsersDTO(Page<User> page) {
        List<UserListDTO> users = page.getContent()
                .stream()
                .map(user -> objectMapper.mapObject().map(user, UserListDTO.class))
                .toList();

        return GetAllUsersDTO.builder()
                .users(users)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public UserDTO loginUser(LoginDTO loginDTO) {
        log.info("============= Retrieve user ===============");
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ClientException(HttpStatus.BAD_REQUEST, "Email not found."));

        log.info("============= Check user status ===============");
        if (user.getStatus() == AccountStatus.AWAITING_CONFIRMATION) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "User status is not active.");
        }

        log.info("============= Check matching passwords ===============");
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Incorrect password.");
        }

        log.info("============= Enforce temporary password expiry ===============");
        if (user.isMustChangePassword()
                && user.getPasswordResetAt() != null
                && DateUtil.now().isAfter(user.getPasswordResetAt().plusHours(tempPasswordTtlHours))) {
            throw new ClientException(HttpStatus.BAD_REQUEST,
                    "Your temporary password has expired. Please request a new password reset.");
        }

        log.info("============= Assign session token, refresh token and login ===============");
        return UserDTO
                .builder()
                .id(user.getId().toString())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .mustChangePassword(user.isMustChangePassword())
                .accessToken(jwtTokenProvider.generateToken(user.getEmail()))
                .refreshToken(refreshTokenService.createRefreshToken(user.getId().toString()).getToken())
                .build();
    }

    @Override
    public UserDTO activateUserOTP(String phoneNumber, String otp) {
        log.info("============= Checking if user exists ===============");
        User existingUser = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new ClientException(
                        HttpStatus.NOT_FOUND,
                        "User not found.")
                );

        if (existingUser.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "User is already active.");
        }

        log.info("============= Validating OTP ===============");
        otpService.validateOTPInternal(phoneNumber, otp);

        existingUser.setStatus(AccountStatus.ACTIVE);
        existingUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(existingUser);
        emailService.sendWelcomeEmail(existingUser);

        log.info("============= User confirmation successful ===============");
        return objectMapper.mapObject().map(existingUser, UserDTO.class);
    }

    @Override
    public ResetPwdDTO updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        log.info("============= Checking if user exists ===============");
        User existingUser = userRepository.findByEmail(updatePasswordDTO.getEmail())
                .orElseThrow(() -> new ClientException(HttpStatus.NOT_FOUND, "Account does not exist."));

        log.info("============= Check if active user or awaiting confirmation ===============");
        if (existingUser.getStatus().equals(AccountStatus.AWAITING_CONFIRMATION)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Activate user before resetting password.");
        }

        log.info("============= Check if current password match ===============");
        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), existingUser.getPassword())) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Current password matches old password.");
        }

        log.info("============= Save new password ===============");
        existingUser.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        existingUser.setMustChangePassword(false);
        existingUser.setPasswordResetAt(null);

        log.info("============= Save user ===============");
        userRepository.save(existingUser);
        return ResetPwdDTO.builder()
                .username(existingUser.getEmail())
                .pwdReset(true)
                .build();
    }

    @Override
    public ResetPwdDTO adminResetPassword(ObjectId userId) {
        log.info("============= Admin password reset for user {} ===============", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ClientException(HttpStatus.NOT_FOUND, "User not found."));

        log.info("============= Check if active user or awaiting confirmation ===============");
        if (existingUser.getStatus().equals(AccountStatus.AWAITING_CONFIRMATION)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Activate user before resetting password.");
        }

        String temporaryPassword = GenerateUtil.generateTempPassword();

        // Deliver the temporary password BEFORE persisting it. If the email fails we abort
        // without touching the stored password, so the user is never locked out.
        log.info("============= Email temporary password to user ===============");
        try {
            emailService.sendPasswordResetEmail(existingUser, temporaryPassword);
        } catch (EmailException e) {
            log.error("Password reset aborted — could not email temporary password to {}: {}",
                    existingUser.getEmail(), e.getMessage());
            throw new ClientException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not send the password reset email. The password was not changed; please try again.");
        }

        log.info("============= Save temporary password and force change on next login ===============");
        existingUser.setPassword(passwordEncoder.encode(temporaryPassword));
        existingUser.setMustChangePassword(true);
        existingUser.setPasswordResetAt(DateUtil.now());
        existingUser.setUpdatedAt(DateUtil.now());
        userRepository.save(existingUser);

        return ResetPwdDTO.builder()
                .username(existingUser.getEmail())
                .pwdReset(true)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Logout called with no refresh token");
            return;
        }
        refreshTokenRepository.deleteByToken(refreshToken);
        log.info("====================User logged out=========================: ");
    }

    @Override
    public UserDTO addRole(ObjectId userId, String role) {
        log.info("============= Add role {} to user {} ===============", role, userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ClientException(HttpStatus.NOT_FOUND, "User not found."));

        String normalizeRole = normalizeRole(role);

        if (user.getRoles() != null && user.getRoles().contains(normalizeRole)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "User already has role:" + normalizeRole);
        }

        user.addRoles(normalizeRole);
        user.setUpdatedAt(DateUtil.now());
        userRepository.save(user);

        log.info("Role {} added to user {}", normalizeRole, user);
        return objectMapper.mapObject().map(user, UserDTO.class);
    }

    @Override
    public UserDTO removeRole(ObjectId userId, String role) {
        log.info("============= Removing role {} from user {} ===============", role, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientException(
                        HttpStatus.NOT_FOUND, "User not found."));

        String normalizedRole = normalizeRole(role);

        if (user.getRoles() == null || !user.getRoles().contains(normalizedRole)) {
            throw new ClientException(
                    HttpStatus.BAD_REQUEST,
                    "User does not have role: " + normalizedRole);
        }

        if (user.getRoles().size() == 1) {
            throw new ClientException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot remove the last role from a user. Assign another role first.");
        }

        user.removeRoles(normalizedRole);
        user.setUpdatedAt(DateUtil.now());
        userRepository.save(user);

        log.info("Role {} removed from user {}", normalizedRole, user);
        return objectMapper.mapObject().map(user, UserDTO.class);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new ClientException(
                    HttpStatus.BAD_REQUEST, "Role cannot be null or empty.");
        }
        return role.toUpperCase().startsWith("ROLE_")
                ? role.toUpperCase()
                : "ROLE_" + role.toUpperCase();
    }
}
