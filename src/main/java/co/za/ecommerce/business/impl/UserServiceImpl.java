package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.business.RefreshTokenService;
import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.user.*;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.RefreshTokenRepository;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import co.za.ecommerce.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OTPService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
        log.info("============= OTP {} ===============", otpService.generateOTP(user.getPhone()));
        return user;
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

        log.info("============= Assign session token, refresh token and login ===============");
        return UserDTO
                .builder()
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .phone(user.getPhone())
                .role(user.getRoles())
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

        log.info("============= Save user ===============");
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
    }
}
