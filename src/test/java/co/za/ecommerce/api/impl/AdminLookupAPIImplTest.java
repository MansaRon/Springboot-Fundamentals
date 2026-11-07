package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.LookupException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminLookupAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminLookupAPI Controller Tests")
class AdminLookupAPIImplTest {

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

    private static final String LOOKUP_ID = "507f1f77bcf86cd799439011";
    private static final String BODY = """
            {"type":"DELIVERY_OPTION","code":"EXPRESS","label":"Express Delivery","active":true,"sortOrder":2}
            """;

    private LookupValueDTO sampleResponse() {
        return LookupValueDTO.builder()
                .id(LOOKUP_ID)
                .type(LookupType.DELIVERY_OPTION)
                .code("EXPRESS")
                .label("Express Delivery")
                .active(true)
                .sortOrder(2)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/admin/lookup")
    class Create {
        @Test
        @DisplayName("shouldReturn201WithCreatedValue")
        void shouldReturn201WithCreatedValue() throws Exception {
            when(lookupService.create(any(LookupValueDTO.class))).thenReturn(sampleResponse());

            mockMvc.perform(post("/api/v1/admin/lookup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Lookup value created."))
                    .andExpect(jsonPath("$.data.code").value("EXPRESS"));

            verify(lookupService).create(any(LookupValueDTO.class));
        }

        @Test
        @DisplayName("shouldReturn400WhenRequiredFieldsMissing")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            // Missing type/code/label → bean validation rejects before the service is hit.
            mockMvc.perform(post("/api/v1/admin/lookup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sortOrder\":1}"))
                    .andExpect(status().isBadRequest());

            verify(lookupService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/lookup/{id}")
    class Update {
        @Test
        @DisplayName("shouldReturn200WithUpdatedValue")
        void shouldReturn200WithUpdatedValue() throws Exception {
            when(lookupService.update(eq(new org.bson.types.ObjectId(LOOKUP_ID)), any(LookupValueDTO.class)))
                    .thenReturn(sampleResponse());

            mockMvc.perform(put("/api/v1/admin/lookup/{id}", LOOKUP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Lookup value updated."))
                    .andExpect(jsonPath("$.data.code").value("EXPRESS"));
        }

        @Test
        @DisplayName("shouldReturn404WhenValueNotFound")
        void shouldReturn404WhenValueNotFound() throws Exception {
            when(lookupService.update(any(), any(LookupValueDTO.class)))
                    .thenThrow(new LookupException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Lookup value not found",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(put("/api/v1/admin/lookup/{id}", LOOKUP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Lookup value not found"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/lookup/{id}")
    class Delete {
        @Test
        @DisplayName("shouldReturn200WhenDeleted")
        void shouldReturn200WhenDeleted() throws Exception {
            doNothing().when(lookupService).delete(eq(new org.bson.types.ObjectId(LOOKUP_ID)));

            mockMvc.perform(delete("/api/v1/admin/lookup/{id}", LOOKUP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Lookup value deleted."));

            verify(lookupService).delete(eq(new org.bson.types.ObjectId(LOOKUP_ID)));
        }

        @Test
        @DisplayName("shouldReturn404WhenValueNotFound")
        void shouldReturn404WhenValueNotFound() throws Exception {
            doThrow(new LookupException(
                    HttpStatus.NOT_FOUND.toString(),
                    "Lookup value not found",
                    HttpStatus.NOT_FOUND.value()))
                    .when(lookupService).delete(any());

            mockMvc.perform(delete("/api/v1/admin/lookup/{id}", LOOKUP_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Lookup value not found"));
        }
    }
}
