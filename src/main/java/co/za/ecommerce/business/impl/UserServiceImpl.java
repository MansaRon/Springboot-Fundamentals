package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.user.UserCreateDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
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

    //private PasswordEncoder passwordEncoder;

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
                .password(userCreateDTO.getPwd())
                .build();

        user.addRoles("ROLE_USER");
        log.info("============= Saving user ===============");
        userRepository.save(user);
        return user;
    }

    @Override
    public UserDTO loginUser(String email, String password) {
        return null;
    }

    @Override
    public UserDTO activateUser(String email, String otp) {
        log.info("============= Checking if user exists ===============");
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ClientException(
                        HttpStatus.NOT_FOUND,
                        "User not found.")
                );

        // Check if the user's status is already active
        if (existingUser.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "User is already active.");
        }

        // Verify the OTP
        if (!otpService.validateOTP(email, otp)) { // otpService is a mockable service for OTP management
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
