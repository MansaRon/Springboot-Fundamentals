package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.coupon.CouponDTO;
import co.za.ecommerce.exception.CouponException;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCouponAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminCouponAPI Controller Tests")
class AdminCouponAPIImplTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CouponService couponService;
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

    private static final String COUPON_ID = "507f1f77bcf86cd799439011";
    private static final String BODY = """
            {"code":"SAVE50","amount":50,"minSpend":200,"expiresAt":"2099-01-01T00:00:00","usageLimit":100,"active":true}
            """;

    private CouponDTO sample() {
        return CouponDTO.builder().code("SAVE50").amount(50).minSpend(200).usageLimit(100).active(true).build();
    }

    @Nested
    @DisplayName("POST /api/v1/admin/coupons")
    class Create {
        @Test
        @DisplayName("shouldReturn201WithCreatedCoupon")
        void shouldReturn201() throws Exception {
            when(couponService.create(any(CouponDTO.class))).thenReturn(sample());

            mockMvc.perform(post("/api/v1/admin/coupons")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Coupon created."))
                    .andExpect(jsonPath("$.data.code").value("SAVE50"));
        }

        @Test
        @DisplayName("shouldReturn400WhenRequiredFieldsMissing")
        void shouldReturn400WhenInvalid() throws Exception {
            mockMvc.perform(post("/api/v1/admin/coupons")
                            .contentType(MediaType.APPLICATION_JSON).content("{\"minSpend\":200}"))
                    .andExpect(status().isBadRequest());

            verify(couponService, never()).create(any());
        }

        @Test
        @DisplayName("shouldReturn409OnDuplicateCode")
        void shouldReturn409() throws Exception {
            when(couponService.create(any(CouponDTO.class)))
                    .thenThrow(new CouponException(HttpStatus.CONFLICT.toString(),
                            "A coupon with code 'SAVE50' already exists.", HttpStatus.CONFLICT.value()));

            mockMvc.perform(post("/api/v1/admin/coupons")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("A coupon with code 'SAVE50' already exists."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/coupons")
    class GetAll {
        @Test
        @DisplayName("shouldReturn200WithCoupons")
        void shouldReturn200() throws Exception {
            when(couponService.getAll()).thenReturn(java.util.List.of(sample()));

            mockMvc.perform(get("/api/v1/admin/coupons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].code").value("SAVE50"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/coupons/{id}")
    class Delete {
        @Test
        @DisplayName("shouldReturn200WhenDeleted")
        void shouldReturn200() throws Exception {
            doNothing().when(couponService).delete(any());

            mockMvc.perform(delete("/api/v1/admin/coupons/{id}", COUPON_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Coupon deleted."));
        }
    }
}
