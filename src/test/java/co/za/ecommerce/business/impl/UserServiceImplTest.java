package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.user.LoginDTO;
import co.za.ecommerce.dto.user.UserCreateDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.factories.DTOFactory;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserCreateDTO userCreateDTOTest;
    private User customerUser;

    private LoginDTO validLoginDTO;
    private User activeUser;

    @BeforeEach
    void setUp() {
        // Register Details
        userCreateDTOTest = DTOFactory.userCreateDTO();
        customerUser = User.builder()
                .name(userCreateDTOTest.getName())
                .email(userCreateDTOTest.getEmail())
                .phone(userCreateDTOTest.getPhone())
                .status(AccountStatus.AWAITING_CONFIRMATION)
                .password(userCreateDTOTest.getPwd())
                .roles(new HashSet<>(Arrays.asList("ROLE_USER")))
                .build();

        // Login details
        validLoginDTO = LoginDTO.builder()
                .email("netshi10@gmail.com")
                .password("Joyful95")
                .build();
        activeUser = User.builder()
                .name("Fulufhelo")
                .email("netshi10@gmail.com")
                .password("Joyful95")
                .status(AccountStatus.ACTIVE)
                .roles(Set.of("ROLE_USER"))
                .phone("0815126811")
                .build();
    }

    private void assetUser(UserCreateDTO expected, User actual) {
        assertNotNull(actual);

        assertNotNull(actual.getId());
        assertNotNull(actual.getCreatedAt());

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createUser() {
        // Mock the repository behavior
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(new ObjectId()); // Simulate database assigning an ID
            user.setCreatedAt(LocalDateTime.now()); // Simulate database timestamp
            return user;
        });

        // Call the service method
        User savedUser = userService.createUser(userCreateDTOTest);

        // Assert the saved user is not null
        assertThat(savedUser).isNotNull();
        assertNotNull(savedUser.getId(), "ID should not be null");
        assertNotNull(savedUser.getCreatedAt(), "CreatedAt should not be null");

        // Assert the fields are correctly mapped
        assetUser(userCreateDTOTest, savedUser);
    }

    @Test
    void loginUserSuccessful() {
        given(userRepository.findByEmail(validLoginDTO.getEmail()))
                .willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches(validLoginDTO.getPassword(), activeUser.getPassword()))
                .willReturn(true);
        //given(jwtTokenProvider.generateToken(activeUser.getEmail()))
        //        .willReturn(validLoginDTO.getEmail());

        UserDTO userDTO = userService.loginUser(validLoginDTO);

        assertNotNull(userDTO);
        assertEquals(activeUser.getName(), userDTO.getName());
        assertEquals(activeUser.getEmail(), userDTO.getEmail());
        assertEquals(activeUser.getStatus().name(), userDTO.getStatus());
        assertEquals(activeUser.getPhone(), userDTO.getPhone());
        //assertEquals("mocked-jwt-token", userDTO.getAccessToken());
        verify(userRepository).findByEmail(validLoginDTO.getEmail());
        verify(passwordEncoder).matches(validLoginDTO.getPassword(), activeUser.getPassword());
        //verify(jwtTokenProvider).generateToken(activeUser.getEmail());
    }

}