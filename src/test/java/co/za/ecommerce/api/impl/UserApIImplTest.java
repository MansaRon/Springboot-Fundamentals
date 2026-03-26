package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.user.*;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserApIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserApIImpl Controller Tests")
class UserApIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private ObjectMapper objectMapper;

    @MockBean private OTPService otpService;
    @MockBean private ProductService productService;
    @MockBean private CartService cartService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private OrderService orderService;
    @MockBean private S3Service s3Service;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private WishlistService wishlistService;
    @MockBean private ModelMapper modelMapper;

    private UserDTO userDTO;
    private UserCreateDTO userCreateDTO;

    @BeforeEach
    void setUp() {
        userDTO = UserDTO.builder()
                .name("User")
                .email("email@example.com")
                .phone("0821234567")
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test.token")
                .refreshToken("refresh-token-value")
                .status("ACTIVE")
                .role(Set.of("ROLE_USER"))
                .build();

        userCreateDTO = UserCreateDTO.builder()
                .name("User")
                .email("email@example.com")
                .phone("0821234567")
                .pwd("SecurePass123@")
                .build();
    }

    @Test
    @DisplayName("shouldReturn200WithHelloWorldMessage")
    void shouldReturn200WithHelloWorldMessage() throws Exception {
        mockMvc.perform(get("/api/v1/auth/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {
        @Test
        @DisplayName("shouldReturn400WhenEmailAlreadyExists")
        void shouldReturn400WhenEmailAlreadyExists() throws Exception {
            when(userService.createUser(any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "Email already exists."));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "User",
                                      "email": "email@example.com",
                                      "phone": "0821234567",
                                      "pwd": "SecurePass123@"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email already exists."));
        }

        @Test
        @DisplayName("shouldReturn400WhenPhoneNumberAlreadyExists")
        void shouldReturn400WhenPhoneNumberAlreadyExists() throws Exception {
            when(userService.createUser(any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "Phone number already exists."));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Thendo Makhado",
                                      "email": "thendo@example.com",
                                      "phone": "0821234567",
                                      "pwd": "SecurePass123@"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Phone number already exists."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/confirm/{phoneNum}/{OTP}")
    class ConfirmUser {
        @Test
        @DisplayName("shouldPassCorrectPhoneAndOTPToService")
        void shouldPassCorrectPhoneAndOTPToService() throws Exception {
            when(userService.activateUserOTP(any(), any())).thenReturn(userDTO);

            mockMvc.perform(post("/api/v1/auth/confirm/{phoneNum}/{OTP}",
                    "0821234567", "483920"));

            verify(userService).activateUserOTP(eq("0821234567"), eq("483920"));
        }

        @Test
        @DisplayName("shouldReturn400WhenUserIsAlreadyActive")
        void shouldReturn400WhenUserIsAlreadyActive() throws Exception {
            when(userService.activateUserOTP(any(), any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "User is already active."));

            mockMvc.perform(post("/api/v1/auth/confirm/{phoneNum}/{OTP}",
                            "0821234567", "483920"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User is already active."));
        }

        @Test
        @DisplayName("shouldReturn400WhenOTPIsInvalidOrExpired")
        void shouldReturn400WhenOTPIsInvalidOrExpired() throws Exception {
            when(userService.activateUserOTP(any(), any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "Invalid or expired OTP."));

            mockMvc.perform(post("/api/v1/auth/confirm/{phoneNum}/{OTP}",
                            "0821234567", "000000"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid or expired OTP."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {
        @Test
        @DisplayName("shouldReturn400WhenPasswordIsIncorrect")
        void shouldReturn400WhenPasswordIsIncorrect() throws Exception {
            when(userService.loginUser(any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "Invalid email or password."));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "thendo@example.com",
                                      "password": "WrongPassword123@"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid email or password."));
        }

        @Test
        @DisplayName("shouldReturn400WhenAccountIsNotActivated")
        void shouldReturn400WhenAccountIsNotActivated() throws Exception {
            when(userService.loginUser(any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST,
                            "Account is not activated. Please verify your OTP."));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "thendo@example.com",
                                      "password": "SecurePass123@"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Account is not activated. Please verify your OTP."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/update-password")
    class UpdatePassword {
        @Test
        @DisplayName("shouldReturn400WhenCurrentPasswordIsIncorrect")
        void shouldReturn400WhenCurrentPasswordIsIncorrect() throws Exception {
            when(userService.updatePassword(any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "Current password is incorrect."));

            mockMvc.perform(post("/api/v1/auth/update-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "email@example.com",
                                      "currentPassword": "WrongOld123@",
                                      "newPassword": "NewPass456@"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Current password is incorrect."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {
        @Test
        @DisplayName("shouldReturn200WhenLogoutIsSuccessful")
        void shouldReturn200WhenLogoutIsSuccessful() throws Exception {
            doNothing().when(userService).logout(eq("refresh-token-value"));

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "refreshToken": "refresh-token-value"
                                    }
                                    """))
                    .andExpect(status().isOk());

            verify(userService).logout(eq("refresh-token-value"));
        }

        @Test
        @DisplayName("shouldPassRefreshTokenToService")
        void shouldPassRefreshTokenToService() throws Exception {
            doNothing().when(userService).logout(any());

            mockMvc.perform(post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "refreshToken": "my-specific-refresh-token"
                            }
                            """));

            verify(userService).logout(eq("my-specific-refresh-token"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshToken {
        @Test
        @DisplayName("shouldReturn200WithNewAccessTokenWhenRefreshTokenIsValid")
        void shouldReturn200WithNewAccessTokenWhenRefreshTokenIsValid() throws Exception {
            TokenRefreshResponse tokenResponse = TokenRefreshResponse.builder()
                    .accessToken("new-access-token")
                    .refreshToken("refresh-token-value")
                    .build();

            when(refreshTokenService.refreshAccessToken(eq("refresh-token-value")))
                    .thenReturn(tokenResponse);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "refreshToken": "refresh-token-value"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Token refreshed"))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-value"));
        }

        @Test
        @DisplayName("shouldReturn400WhenRefreshTokenIsExpired")
        void shouldReturn400WhenRefreshTokenIsExpired() throws Exception {
            when(refreshTokenService.refreshAccessToken(any()))
                    .thenThrow(new ClientException(
                            HttpStatus.BAD_REQUEST, "Refresh token has expired."));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "refreshToken": "expired-refresh-token"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Refresh token has expired."));
        }

        @Test
        @DisplayName("shouldPassCorrectRefreshTokenToService")
        void shouldPassCorrectRefreshTokenToService() throws Exception {
            when(refreshTokenService.refreshAccessToken(any()))
                    .thenReturn(TokenRefreshResponse.builder()
                            .accessToken("new-token")
                            .refreshToken("my-refresh")
                            .build());

            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "refreshToken": "my-refresh"
                            }
                            """));

            verify(refreshTokenService).refreshAccessToken(eq("my-refresh"));
        }
    }
}