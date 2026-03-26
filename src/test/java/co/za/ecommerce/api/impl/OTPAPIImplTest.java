package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.otp.OTPResponseDTO;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.OTPException;
import co.za.ecommerce.filter.JwtAuthenticationFilter;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OTPAPIImpl.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("OTPAPI Controller Tests")
@AutoConfigureMockMvc(addFilters = false)
class OTPAPIImplTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OTPService otpService;
    @MockBean private UserService userService;
    @MockBean private ProductService productService;
    @MockBean private CartService cartService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private OrderService orderService;
    @MockBean private WishlistService wishlistService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    // @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private OTPResponseDTO generateResponseDTO;
    private OTPResponseDTO validateResponseDTO;

    @BeforeEach
    void setUp() {
        generateResponseDTO = OTPResponseDTO.builder()
                .phoneNumber("0821234567")
                .expiryMinutes(5)
                .build();

        validateResponseDTO = OTPResponseDTO.builder()
                .phoneNumber("0821234567")
                .valid(true)
                .build();
    }

    @Nested
    // To keep security for future tests, use @WithMockUser
    // OR, if endpoint has permitAll on securityconfig, addFilters = false works as well. No need to simulate auth
    @DisplayName("POST /api/v1/otp/generate")
    class GenerateOTP {
        @Test
        @DisplayName("shouldReturn200WithOTPResponseWhenPhoneNumberIsValid")
        void shouldReturn200WithOTPResponseWhenPhoneNumberIsValid() throws Exception {
            when(otpService.generateOTP(eq("0821234567"))).thenReturn(generateResponseDTO);

            mockMvc.perform(post("/api/v1/otp/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("OTP sent successfully. Valid for 5 minutes."))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.data.phoneNumber").value("0821234567"))
                    .andExpect(jsonPath("$.data.expiryMinutes").value(5));
        }

        @Test
        @DisplayName("shouldReturn400WhenPhoneNumberIsMissing")
        void shouldReturn400WhenPhoneNumberIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/otp/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("shouldReturn400WhenRequestBodyIsEmpty")
        void shouldReturn400WhenRequestBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v1/otp/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("shouldReturn400WhenServiceThrowsOTPException")
        void shouldReturn400WhenServiceThrowsOTPException() throws Exception {
            when(otpService.generateOTP(eq("0821234567")))
                    .thenThrow(new OTPException(HttpStatus.BAD_REQUEST, "No OTP found."));

            mockMvc.perform(post("/api/v1/otp/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No OTP found."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/otp/resend")
    class ResendOTP {
        @Test
        @DisplayName("shouldReturn200WithNewOTPResponseWhenPhoneNumberIsValid")
        void shouldReturn200WithNewOTPResponseWhenPhoneNumberIsValid() throws Exception {
            when(otpService.resendOTP(eq("0821234567"))).thenReturn(generateResponseDTO);

            mockMvc.perform(post("/api/v1/otp/resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("New OTP sent successfully. Valid for 5 minutes."))
                    .andExpect(jsonPath("$.data.phoneNumber").value("0821234567"));
        }

        @Test
        @DisplayName("shouldReturn400WhenPhoneNumberIsMissing")
        void shouldReturn400WhenPhoneNumberIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/otp/resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("shouldReturn400WhenServiceThrowsOTPException")
        void shouldReturn400WhenServiceThrowsOTPException() throws Exception {
            when(otpService.resendOTP(eq("0821234567"))).thenThrow(new OTPException(HttpStatus.BAD_REQUEST, "OTP has expired."));

            mockMvc.perform(post("/api/v1/otp/resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("OTP has expired."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/otp/validate")
    class ValidateOTP {
        @Test
        @DisplayName("shouldReturn200WithValidTrueWhenOTPIsCorrect")
        void shouldReturn200WithValidTrueWhenOTPIsCorrect() throws Exception {
            when(otpService.validateOTP(eq("0821234567"), eq("483920"))).thenReturn(validateResponseDTO);

            mockMvc.perform(post("/api/v1/otp/validate").contentType(MediaType.APPLICATION_JSON).content("{\"phoneNumber\": \"0821234567\", \"otp\": \"483920\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("OTP validated successfully."))
                    .andExpect(jsonPath("$.data.valid").value(true))
                    .andExpect(jsonPath("$.data.phoneNumber").value("0821234567"));
        }

        @Test
        @DisplayName("shouldReturn400WhenPhoneNumberIsMissing")
        void shouldReturn400WhenPhoneNumberIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/otp/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"otp\": \"483920\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("shouldReturn400WhenOTPIsMissing")
        void shouldReturn400WhenOTPIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/otp/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("shouldReturn400WhenOTPIsIncorrect")
        void shouldReturn400WhenOTPIsIncorrect() throws Exception {
            when(otpService.validateOTP(eq("0821234567"), eq("000000")))
                    .thenThrow(new OTPException(HttpStatus.BAD_REQUEST, "Invalid OTP. 2 attempt(s) remaining."));

            mockMvc.perform(post("/api/v1/otp/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\", \"otp\": \"000000\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid OTP. 2 attempt(s) remaining."));
        }

        @Test
        @DisplayName("shouldReturn400WhenOTPHasExpired")
        void shouldReturn400WhenOTPHasExpired() throws Exception {
            when(otpService.validateOTP(eq("0821234567"), eq("483920")))
                    .thenThrow(new OTPException(HttpStatus.BAD_REQUEST, "OTP has expired. Please request a new OTP."));

            mockMvc.perform(post("/api/v1/otp/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\", \"otp\": \"483920\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("OTP has expired. Please request a new OTP."));
        }

        @Test
        @DisplayName("shouldReturn400WhenMaxAttemptsReached")
        void shouldReturn400WhenMaxAttemptsReached() throws Exception {
            when(otpService.validateOTP(eq("0821234567"), eq("000000")))
                    .thenThrow(new OTPException(HttpStatus.BAD_REQUEST, "Too many failed attempts. OTP invalidated. Please request a new OTP."));

            mockMvc.perform(post("/api/v1/otp/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneNumber\": \"0821234567\", \"otp\": \"000000\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Too many failed attempts. OTP invalidated. Please request a new OTP."));
        }
    }
}