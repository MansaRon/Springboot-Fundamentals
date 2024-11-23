package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.user.UserCreateDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        if (userRepository.existsByPhone(userCreateDTO.getPhone())) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "Phone number already exists.");
        }

        User user = objectMapper.mapObject().map(userCreateDTO, User.class);
        user.addRoles("ROLE_USER");
        user = User.builder()
                .name(userCreateDTO.getName())
                .email(userCreateDTO.getEmail())
                .phone(userCreateDTO.getPhone())
                .status(User.Status.AWAITING_CONFIRMATION)
                .password(passwordEncoder.encode(userCreateDTO.getPwd()))
                .build();

        userRepository.save(user);
        return user;
    }

    @Override
    public UserDTO loginUser(String email, String password) {
        return null;
    }
}
