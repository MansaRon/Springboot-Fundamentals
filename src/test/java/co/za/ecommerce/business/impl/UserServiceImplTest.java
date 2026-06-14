package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.EmailService;
import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.business.RefreshTokenService;
import co.za.ecommerce.dto.user.*;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.RefreshToken;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.RefreshTokenRepository;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.za.ecommerce.dto.otp.OTPResponseDTO;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private OTPService otpService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EmailService emailService;
    @InjectMocks private UserServiceImpl userService;

    private User user;
    private ObjectId userId;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        user = TestDataBuilder.buildUser(userId);
        user.setStatus(AccountStatus.ACTIVE);
        user.setPassword("encoded-password");
        user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
    }

    @Nested
    @DisplayName("CreateUser")
    class CreateUser {

        @Test
        @DisplayName("shouldCreateUserSuccessfullyWhenEmailAndPhoneAreUnique")
        void shouldCreateUserSuccessfullyWhenEmailAndPhoneAreUnique() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPhone(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(otpService.generateOTP(anyString())).thenReturn(new OTPResponseDTO());

            UserCreateDTO dto = new UserCreateDTO();
            dto.setName("Thendo");
            dto.setEmail("thendo@example.com");
            dto.setPhone("0821234567");
            dto.setPwd("SecurePass@1");

            User result = userService.createUser(dto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("thendo@example.com");
            verify(userRepository).save(any(User.class));
            verify(otpService, times(1)).generateOTP("0821234567");
        }

        @Test
        @DisplayName("shouldThrowWhenEmailAlreadyExists")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            UserCreateDTO dto = new UserCreateDTO();
            dto.setEmail("existing@example.com");
            dto.setPhone("0821234567");

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenPhoneAlreadyExists")
        void shouldThrowWhenPhoneAlreadyExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPhone(anyString())).thenReturn(true);

            UserCreateDTO dto = new UserCreateDTO();
            dto.setEmail("new@example.com");
            dto.setPhone("0821234567");

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Phone number already exists");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("LoginUser")
    class LoginUser {

        @Test
        @DisplayName("shouldReturnUserDTOWithTokensOnSuccessfulLogin")
        void shouldReturnUserDTOWithTokensOnSuccessfulLogin() {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail("email@example.com");
            loginDTO.setPassword("SecurePass123@");

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("refresh-token-value");
            refreshToken.setExpiry(new Date(System.currentTimeMillis() + 100000));

            when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtTokenProvider.generateToken(anyString())).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

            UserDTO result = userService.loginUser(loginDTO);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token-value");
        }

        @Test
        @DisplayName("shouldThrowWhenEmailNotFound")
        void shouldThrowWhenEmailNotFound() {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail("unknown@example.com");
            loginDTO.setPassword("pass");

            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.loginUser(loginDTO))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Email not found");
        }

        @Test
        @DisplayName("shouldThrowWhenUserIsAwaitingConfirmation")
        void shouldThrowWhenUserIsAwaitingConfirmation() {
            user.setStatus(AccountStatus.AWAITING_CONFIRMATION);

            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail("email@example.com");
            loginDTO.setPassword("pass");

            when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.loginUser(loginDTO))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("shouldThrowWhenPasswordDoesNotMatch")
        void shouldThrowWhenPasswordDoesNotMatch() {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail("email@example.com");
            loginDTO.setPassword("WrongPass");

            when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongPass", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userService.loginUser(loginDTO))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Incorrect password");
        }
    }

    @Nested
    @DisplayName("ActivateUserOTP")
    class ActivateUserOTP {

        @Test
        @DisplayName("shouldActivateUserWhenOTPIsValid")
        void shouldActivateUserWhenOTPIsValid() {
            user.setStatus(AccountStatus.AWAITING_CONFIRMATION);

            org.modelmapper.ModelMapper modelMapper = mock(org.modelmapper.ModelMapper.class);
            when(userRepository.findByPhone("0821234567")).thenReturn(Optional.of(user));
            when(otpService.validateOTPInternal(anyString(), anyString())).thenReturn(true);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(), any())).thenReturn(new UserDTO());

            UserDTO result = userService.activateUserOTP("0821234567", "123456");

            assertThat(result).isNotNull();
            assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("shouldThrowWhenUserNotFoundByPhone")
        void shouldThrowWhenUserNotFoundByPhone() {
            when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.activateUserOTP("0000000000", "123456"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("shouldThrowWhenUserIsAlreadyActive")
        void shouldThrowWhenUserIsAlreadyActive() {
            user.setStatus(AccountStatus.ACTIVE);
            when(userRepository.findByPhone("0821234567")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.activateUserOTP("0821234567", "123456"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("already active");
        }
    }

    @Nested
    @DisplayName("UpdatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("shouldUpdatePasswordSuccessfully")
        void shouldUpdatePasswordSuccessfully() {
            UpdatePasswordDTO dto = new UpdatePasswordDTO();
            dto.setEmail("email@example.com");
            dto.setCurrentPassword("OldPass@1");
            dto.setNewPassword("NewPass@1");

            when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("OldPass@1", user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("NewPass@1")).thenReturn("new-encoded");

            ResetPwdDTO result = userService.updatePassword(dto);

            assertThat(result.isPwdReset()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("shouldThrowWhenUserAccountNotFound")
        void shouldThrowWhenUserAccountNotFound() {
            UpdatePasswordDTO dto = new UpdatePasswordDTO();
            dto.setEmail("unknown@example.com");

            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updatePassword(dto))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Account does not exist");
        }

        @Test
        @DisplayName("shouldThrowWhenUserIsAwaitingConfirmation")
        void shouldThrowWhenUserIsAwaitingConfirmation() {
            user.setStatus(AccountStatus.AWAITING_CONFIRMATION);
            UpdatePasswordDTO dto = new UpdatePasswordDTO();
            dto.setEmail("email@example.com");

            when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.updatePassword(dto))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Activate user before resetting password");
        }

        @Test
        @DisplayName("shouldThrowWhenCurrentPasswordDoesNotMatch")
        void shouldThrowWhenCurrentPasswordDoesNotMatch() {
            UpdatePasswordDTO dto = new UpdatePasswordDTO();
            dto.setEmail("email@example.com");
            dto.setCurrentPassword("WrongOld");

            when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongOld", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userService.updatePassword(dto))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Current password matches old password");
        }
    }

    @Nested
    @DisplayName("Logout")
    class Logout {

        @Test
        @DisplayName("shouldDeleteRefreshTokenOnLogout")
        void shouldDeleteRefreshTokenOnLogout() {
            userService.logout("some-refresh-token");
            verify(refreshTokenRepository).deleteByToken("some-refresh-token");
        }

        @Test
        @DisplayName("shouldReturnEarlyWhenRefreshTokenIsNull")
        void shouldReturnEarlyWhenRefreshTokenIsNull() {
            userService.logout(null);
            verify(refreshTokenRepository, never()).deleteByToken(any());
        }

        @Test
        @DisplayName("shouldReturnEarlyWhenRefreshTokenIsBlank")
        void shouldReturnEarlyWhenRefreshTokenIsBlank() {
            userService.logout("   ");
            verify(refreshTokenRepository, never()).deleteByToken(any());
        }
    }

    @Nested
    @DisplayName("AddRole")
    class AddRole {

        @Test
        @DisplayName("shouldAddRoleSuccessfully")
        void shouldAddRoleSuccessfully() {
            user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            org.modelmapper.ModelMapper modelMapper = mock(org.modelmapper.ModelMapper.class);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(), any())).thenReturn(new UserDTO());

            userService.addRole(userId, "ADMIN");

            assertThat(user.getRoles()).contains("ROLE_ADMIN");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("shouldAddRoleWithRolePrefixAlreadyPresent")
        void shouldAddRoleWithRolePrefixAlreadyPresent() {
            user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            org.modelmapper.ModelMapper modelMapper = mock(org.modelmapper.ModelMapper.class);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(), any())).thenReturn(new UserDTO());

            userService.addRole(userId, "ROLE_ADMIN");

            assertThat(user.getRoles()).contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("shouldThrowWhenUserNotFoundForAddRole")
        void shouldThrowWhenUserNotFoundForAddRole() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.addRole(userId, "ADMIN"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("shouldThrowWhenUserAlreadyHasRole")
        void shouldThrowWhenUserAlreadyHasRole() {
            user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.addRole(userId, "USER"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("User already has role");
        }

        @Test
        @DisplayName("shouldThrowWhenRoleIsNull")
        void shouldThrowWhenRoleIsNull() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.addRole(userId, null))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Role cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("RemoveRole")
    class RemoveRole {

        @Test
        @DisplayName("shouldRemoveRoleSuccessfully")
        void shouldRemoveRoleSuccessfully() {
            user.setRoles(new HashSet<>(Set.of("ROLE_USER", "ROLE_ADMIN")));
            org.modelmapper.ModelMapper modelMapper = mock(org.modelmapper.ModelMapper.class);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(), any())).thenReturn(new UserDTO());

            userService.removeRole(userId, "ADMIN");

            assertThat(user.getRoles()).doesNotContain("ROLE_ADMIN");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("shouldThrowWhenUserNotFoundForRemoveRole")
        void shouldThrowWhenUserNotFoundForRemoveRole() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.removeRole(userId, "ADMIN"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("shouldThrowWhenUserDoesNotHaveRole")
        void shouldThrowWhenUserDoesNotHaveRole() {
            user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.removeRole(userId, "ADMIN"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("User does not have role");
        }

        @Test
        @DisplayName("shouldThrowWhenRemovingLastRole")
        void shouldThrowWhenRemovingLastRole() {
            user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.removeRole(userId, "USER"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Cannot remove the last role");
        }
    }
}