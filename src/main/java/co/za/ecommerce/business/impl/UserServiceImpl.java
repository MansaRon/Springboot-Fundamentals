package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.user.LoginDTO;
import co.za.ecommerce.dto.user.UserCreateDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
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
        String optGenerated = otpService.generateOTP(user.getPhone());
        log.info("============= OTP {} ===============", optGenerated);
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

        log.info("============= Assign session token and login ===============");
        return UserDTO
                .builder()
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .phone(user.getPhone())
                .role(user.getRoles())
                .accessToken(jwtTokenProvider.generateToken(user.getEmail()))
                .build();
    }

    @Override
    public UserDTO activateUser(String phoneNumber, String otp) {
        log.info("============= Checking if user exists ===============");
        User existingUser = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new ClientException(
                        HttpStatus.NOT_FOUND,
                        "User not found.")
                );

        // Check if the user's status is already active
        if (existingUser.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "User is already active.");
        }

        // Verify the OTP
        if (!otpService.validateOTP(phoneNumber, otp)) { // otpService is a mockable service for OTP management
            throw new ClientException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP.");
        }

        // Update the user status to ACTIVE
        existingUser.setStatus(AccountStatus.ACTIVE);
        existingUser.setUpdatedAt(LocalDateTime.now());

        // Save the updated user
        userRepository.save(existingUser);

        log.info("============= User confirmation successful ===============");
        return objectMapper.mapObject().map(existingUser, UserDTO.class);
    }
}
