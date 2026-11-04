package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.lookup.LookupType;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LookupAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LookupAPI Controller Tests")
class LookupAPIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private LookupService lookupService;

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

    @Nested
    @DisplayName("GET /api/v1/lookup/{type}")
    class GetByType {

        @Test
        @DisplayName("shouldReturn200WithActiveValuesUnderData")
        void shouldReturn200WithActiveValuesUnderData() throws Exception {
            LookupValueDTO gauteng = LookupValueDTO.builder()
                    .id("507f1f77bcf86cd799439011")
                    .type(LookupType.PROVINCE)
                    .code("GAUTENG")
                    .label("Gauteng")
                    .metadata(Map.of())
                    .active(true)
                    .sortOrder(3)
                    .build();

            when(lookupService.getActiveByType(eq(LookupType.PROVINCE)))
                    .thenReturn(List.of(gauteng));

            mockMvc.perform(get("/api/v1/lookup/{type}", "PROVINCE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Lookup values retrieved."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].code").value("GAUTENG"))
                    .andExpect(jsonPath("$.data[0].label").value("Gauteng"))
                    .andExpect(jsonPath("$.data[0].type").value("PROVINCE"));
        }

        @Test
        @DisplayName("shouldAcceptLowercaseTypeAndNormalise")
        void shouldAcceptLowercaseTypeAndNormalise() throws Exception {
            when(lookupService.getActiveByType(eq(LookupType.DELIVERY_OPTION)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/lookup/{type}", "delivery_option"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("shouldReturn400ForUnknownType")
        void shouldReturn400ForUnknownType() throws Exception {
            mockMvc.perform(get("/api/v1/lookup/{type}", "NONSENSE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid lookup type: NONSENSE"));
        }
    }
}
