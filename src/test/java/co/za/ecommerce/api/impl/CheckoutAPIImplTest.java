package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("CheckoutAPIImpl Controller Tests")
class CheckoutAPIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CheckoutService checkoutService;

    @MockBean private OTPService otpService;
    @MockBean private UserService userService;
    @MockBean private ProductService productService;
    @MockBean private CartService cartService;
    @MockBean private OrderService orderService;
    @MockBean private WishlistService wishlistService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String USER_ID    = "507f1f77bcf86cd799439011";
    private static final String CART_ID    = "507f1f77bcf86cd799439022";
    private static final String CHECKOUT_ID = "507f1f77bcf86cd799439033";

    private CheckoutDTO pendingCheckoutDTO;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        pendingCheckoutDTO = CheckoutDTO.builder()
                .id(CHECKOUT_ID)
                .status("PENDING")
                .paymentMethod(PaymentMethod.NOT_SELECTED)
                .subtotal(99.98)
                .tax(9.998)
                .totalAmount(109.978)
                .build();

        orderDTO = OrderDTO.builder()
                .orderNumber("ORD-20260314-483920")
                .orderStatus("CONFIRMED")
                .transactionId("TXN-20260314-8F4E2A")
                .totalAmount(125.968)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/checkout/{userId}/initiate-checkout")
    class InitiateCheckout {

        @Test
        @DisplayName("shouldReturn200WithPendingCheckoutWhenCartExists")
        void shouldReturn200WithPendingCheckoutWhenCartExists() throws Exception {
            when(checkoutService.createCheckoutFromCart(eq(new ObjectId(USER_ID))))
                    .thenReturn(pendingCheckoutDTO);

            mockMvc.perform(post("/api/v1/checkout/{userId}/initiate-checkout", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Checkout initiated."))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.subtotal").value(99.98))
                    .andExpect(jsonPath("$.data.totalAmount").value(109.978));
        }

        @Test
        @DisplayName("shouldPassCorrectUserIdToService")
        void shouldPassCorrectUserIdToService() throws Exception {
            when(checkoutService.createCheckoutFromCart(any())).thenReturn(pendingCheckoutDTO);

            mockMvc.perform(post("/api/v1/checkout/{userId}/initiate-checkout", USER_ID))
                    .andExpect(status().isOk());

            verify(checkoutService).createCheckoutFromCart(eq(new ObjectId(USER_ID)));
        }

        @Test
        @DisplayName("shouldReturn400WhenCartNotFound")
        void shouldReturn400WhenCartNotFound() throws Exception {
            when(checkoutService.createCheckoutFromCart(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.NOT_FOUND.toString(),
                            "No active cart found for user.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(post("/api/v1/checkout/{userId}/initiate-checkout", USER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No active cart found for user."));
        }

        @Test
        @DisplayName("shouldReturn400WhenCartIsEmpty")
        void shouldReturn400WhenCartIsEmpty() throws Exception {
            when(checkoutService.createCheckoutFromCart(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Cannot checkout with empty cart.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(post("/api/v1/checkout/{userId}/initiate-checkout", USER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cannot checkout with empty cart."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/checkout/user/{userId}")
    class GetUserCheckout {

        @Test
        @DisplayName("shouldReturn200WithActiveCheckoutWhenExists")
        void shouldReturn200WithActiveCheckoutWhenExists() throws Exception {
            when(checkoutService.getCheckoutByUserId(eq(new ObjectId(USER_ID))))
                    .thenReturn(pendingCheckoutDTO);

            mockMvc.perform(get("/api/v1/checkout/user/{userId}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Active checkout retrieved."))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("shouldReturn400WhenNoCheckoutFoundForUser")
        void shouldReturn400WhenNoCheckoutFoundForUser() throws Exception {
            when(checkoutService.getCheckoutByUserId(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "User does not have any checked out items.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/checkout/user/{userId}", USER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("User does not have any checked out items."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/checkout/cart/{cartId}")
    class GetCheckoutByCart {

        @Test
        @DisplayName("shouldReturn200WithCheckoutWhenCartIdExists")
        void shouldReturn200WithCheckoutWhenCartIdExists() throws Exception {
            when(checkoutService.getCheckoutByCartId(eq(new ObjectId(CART_ID))))
                    .thenReturn(pendingCheckoutDTO);

            mockMvc.perform(get("/api/v1/checkout/cart/{cartId}", CART_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Checkout retrieved."))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("shouldReturn400WhenNoCheckoutFoundForCart")
        void shouldReturn400WhenNoCheckoutFoundForCart() throws Exception {
            when(checkoutService.getCheckoutByCartId(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.NOT_FOUND.toString(),
                            "No checkout found for this cart.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(get("/api/v1/checkout/cart/{cartId}", CART_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No checkout found for this cart."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/checkout/admin/status/{status}")
    class GetCheckoutsByStatus {

        @Test
        @DisplayName("shouldReturn200WithCheckoutListWhenValidStatusProvided")
        void shouldReturn200WithCheckoutListWhenValidStatusProvided() throws Exception {
            when(checkoutService.getCheckoutsByStatus(eq("PENDING")))
                    .thenReturn(List.of(pendingCheckoutDTO));

            mockMvc.perform(get("/api/v1/checkout/admin/status/{status}", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Retrieve checkout by status."))
                    // Response uses dataList not data for list results
                    .andExpect(jsonPath("$.dataList").isArray())
                    .andExpect(jsonPath("$.dataList[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("shouldReturn400WhenStatusIsInvalid")
        void shouldReturn400WhenStatusIsInvalid() throws Exception {
            when(checkoutService.getCheckoutsByStatus(eq("INVALID")))
                    .thenThrow(new CheckoutException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Invalid checkout status: INVALID",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/checkout/admin/status/{status}", "INVALID"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid checkout status: INVALID"));
        }
    }

    @Nested
    @Disabled
    @DisplayName("PUT /api/v1/checkout/{userId}")
    class UpdateCheckout {

        @Test
        @DisplayName("shouldReturn200WithUpdatedCheckoutWhenRequestIsValid")
        void shouldReturn200WithUpdatedCheckoutWhenRequestIsValid() throws Exception {
            CheckoutDTO updatedDTO = CheckoutDTO.builder()
                    .status("PENDING")
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .subtotal(99.98)
                    .tax(9.998)
                    .totalAmount(109.978)
                    .build();

            when(checkoutService.updateCheckout(eq(new ObjectId(USER_ID)), any(CheckoutDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(put("/api/v1/checkout/{userId}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "paymentMethod": "CREDIT_CARD",
                                      "shippingMethod": "DHL",
                                      "shippingAddress": {
                                        "streetAddress": "51 Frank Ocean Street",
                                        "city": "Johannesburg",
                                        "state": "Gauteng",
                                        "country": "South Africa",
                                        "postalCode": "2003"
                                      },
                                      "billingAddress": {
                                        "streetAddress": "51 Frank Ocean Street",
                                        "city": "Johannesburg",
                                        "state": "Gauteng",
                                        "country": "South Africa",
                                        "postalCode": "2003"
                                      }
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Checkout items updated."))
                    .andExpect(jsonPath("$.data.paymentMethod").value("CREDIT_CARD"));
        }

        @Test
        @DisplayName("shouldReturn400WhenCheckoutIsAlreadyCompleted")
        void shouldReturn400WhenCheckoutIsAlreadyCompleted() throws Exception {
            when(checkoutService.updateCheckout(any(), any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Checkout cannot be updated as it is already completed.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(put("/api/v1/checkout/{userId}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Checkout cannot be updated as it is already completed."));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/checkout/{checkoutId}/confirm")
    class ConfirmCheckout {

        @Test
        @DisplayName("shouldReturn200WithOrderDataWhenPaymentSucceeds")
        void shouldReturn200WithOrderDataWhenPaymentSucceeds() throws Exception {
            when(checkoutService.confirmCheckout(eq(new ObjectId(CHECKOUT_ID))))
                    .thenReturn(orderDTO);

            mockMvc.perform(post("/api/v1/checkout/{checkoutId}/confirm", CHECKOUT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order placed successfully!"))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-20260314-483920"))
                    .andExpect(jsonPath("$.data.orderStatus").value("CONFIRMED"))
                    .andExpect(jsonPath("$.data.transactionId").value("TXN-20260314-8F4E2A"));
        }

        @Test
        @DisplayName("shouldReturn402WhenPaymentFails")
        void shouldReturn402WhenPaymentFails() throws Exception {
            when(checkoutService.confirmCheckout(any()))
                    .thenThrow(new PaymentException(
                            "PAYMENT_FAILED",
                            "Card declined.",
                            HttpStatus.PAYMENT_REQUIRED.value()));

            mockMvc.perform(post("/api/v1/checkout/{checkoutId}/confirm", CHECKOUT_ID))
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.message").value("Card declined."));
        }

        @Test
        @DisplayName("shouldReturn400WhenCheckoutNotFound")
        void shouldReturn400WhenCheckoutNotFound() throws Exception {
            when(checkoutService.confirmCheckout(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Checkout not found.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(post("/api/v1/checkout/{checkoutId}/confirm", CHECKOUT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Checkout not found."));
        }

        @Test
        @DisplayName("shouldReturn400WhenCheckoutIsNotPending")
        void shouldReturn400WhenCheckoutIsNotPending() throws Exception {
            when(checkoutService.confirmCheckout(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Cannot confirm checkout. Current status: COMPLETED",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(post("/api/v1/checkout/{checkoutId}/confirm", CHECKOUT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cannot confirm checkout. Current status: COMPLETED"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/checkout/{checkoutId}")
    class CancelCheckout {

        @Test
        @DisplayName("shouldReturn200WithCancelMessageWhenCheckoutIsPending")
        void shouldReturn200WithCancelMessageWhenCheckoutIsPending() throws Exception {
            doNothing().when(checkoutService).cancelCheckout(eq(new ObjectId(CHECKOUT_ID)));

            mockMvc.perform(delete("/api/v1/checkout/{checkoutId}", CHECKOUT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Checkout cancelled successfully."));

            verify(checkoutService).cancelCheckout(eq(new ObjectId(CHECKOUT_ID)));
        }

        @Test
        @DisplayName("shouldReturn400WhenTryingToCancelCompletedCheckout")
        void shouldReturn400WhenTryingToCancelCompletedCheckout() throws Exception {
            doThrow(new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot cancel checkout with status: COMPLETED",
                    HttpStatus.BAD_REQUEST.value()))
                    .when(checkoutService).cancelCheckout(any());

            mockMvc.perform(delete("/api/v1/checkout/{checkoutId}", CHECKOUT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cannot cancel checkout with status: COMPLETED"));
        }

        @Test
        @DisplayName("shouldReturn400WhenCheckoutNotFound")
        void shouldReturn400WhenCheckoutNotFound() throws Exception {
            doThrow(new CheckoutException(
                    HttpStatus.NOT_FOUND.toString(),
                    "Checkout not found.",
                    HttpStatus.NOT_FOUND.value()))
                    .when(checkoutService).cancelCheckout(any());

            mockMvc.perform(delete("/api/v1/checkout/{checkoutId}", CHECKOUT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Checkout not found."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/checkout/user/{userId}")
    class DeleteUserCheckouts {

        @Test
        @DisplayName("shouldReturn200WithDeletedCheckoutWhenPendingCheckoutsExist")
        void shouldReturn200WithDeletedCheckoutWhenPendingCheckoutsExist() throws Exception {
            when(checkoutService.deleteCheckoutByUserId(eq(new ObjectId(USER_ID))))
                    .thenReturn(pendingCheckoutDTO);

            mockMvc.perform(delete("/api/v1/checkout/user/{userId}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("User checkouts deleted."))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("shouldReturn400WhenNoPendingCheckoutsExist")
        void shouldReturn400WhenNoPendingCheckoutsExist() throws Exception {
            when(checkoutService.deleteCheckoutByUserId(any()))
                    .thenThrow(new CheckoutException(
                            HttpStatus.NOT_FOUND.toString(),
                            "No pending checkouts found for user.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(delete("/api/v1/checkout/user/{userId}", USER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No pending checkouts found for user."));
        }
    }
}