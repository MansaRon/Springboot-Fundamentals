package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.user.UserListDTO;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminUserAPI Controller Tests")
class AdminUserAPIImplTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private OTPService otpService;
    @MockBean private ProductService productService;
    @MockBean private CartService cartService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private OrderService orderService;
    @MockBean private WishlistService wishlistService;
    @MockBean private LookupService lookupService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String USER_ID = "507f1f77bcf86cd799439011";

    private UserListDTO sampleUser(AccountStatus status) {
        return UserListDTO.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .phone("0712345678")
                .status(status.name())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{id}")
    class GetUserById {
        @Test
        @DisplayName("shouldReturn200WithUser")
        void shouldReturn200WithUser() throws Exception {
            when(userService.getUserById(any(ObjectId.class))).thenReturn(sampleUser(AccountStatus.ACTIVE));

            mockMvc.perform(get("/api/v1/admin/users/{id}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User retrieved."))
                    .andExpect(jsonPath("$.data.email").value("jane@example.com"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("shouldReturn404WhenMissing")
        void shouldReturn404WhenMissing() throws Exception {
            when(userService.getUserById(any(ObjectId.class)))
                    .thenThrow(new ClientException(HttpStatus.NOT_FOUND, "User not found."));

            mockMvc.perform(get("/api/v1/admin/users/{id}", USER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User not found."));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/suspend")
    class Suspend {
        @Test
        @DisplayName("shouldSuspendUser")
        void shouldSuspendUser() throws Exception {
            when(userService.updateUserStatus(any(ObjectId.class), eq(AccountStatus.SUSPENDED)))
                    .thenReturn(sampleUser(AccountStatus.SUSPENDED));

            mockMvc.perform(patch("/api/v1/admin/users/{id}/suspend", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User suspended."))
                    .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

            verify(userService).updateUserStatus(any(ObjectId.class), eq(AccountStatus.SUSPENDED));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/unsuspend")
    class Unsuspend {
        @Test
        @DisplayName("shouldReactivateUser")
        void shouldReactivateUser() throws Exception {
            when(userService.updateUserStatus(any(ObjectId.class), eq(AccountStatus.ACTIVE)))
                    .thenReturn(sampleUser(AccountStatus.ACTIVE));

            mockMvc.perform(patch("/api/v1/admin/users/{id}/unsuspend", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User reactivated."))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            verify(userService).updateUserStatus(any(ObjectId.class), eq(AccountStatus.ACTIVE));
        }
    }
}
