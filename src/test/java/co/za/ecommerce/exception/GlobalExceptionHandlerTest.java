package co.za.ecommerce.exception;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getAttribute(PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/api/v1/test");
    }

    // ── Domain exceptions ────────────────────────────────────────────────────

    @Nested
    @DisplayName("ClientException")
    class Client {
        @Test
        void returns400WithMessage() {
            var ex = new ClientException(HttpStatus.BAD_REQUEST, "Email already exists.");
            var response = handler.handleClientException(ex, request);

            assertCommon(response, HttpStatus.BAD_REQUEST, "Email already exists.");
        }
    }

    @Nested
    @DisplayName("OTPException")
    class OTP {
        @Test
        void returns400WithMessage() {
            var ex = new OTPException(HttpStatus.BAD_REQUEST, "Invalid OTP expired.");
            var response = handler.handleOTPException(ex, request);

            assertCommon(response, HttpStatus.BAD_REQUEST, "Invalid OTP expired.");
        }
    }

    @Nested
    @DisplayName("ProductException")
    class Product {
        @Test
        void returnsExceptionStatusWithBody() {
            var ex = new ProductException(HttpStatus.BAD_REQUEST.toString(), "Product not found.", HttpStatus.BAD_REQUEST.value());
            var response = handler.handleProductException(ex, request);

            assertCommon(response, HttpStatus.BAD_REQUEST, "Product not found.");
            assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.toString());
        }
    }

    @Nested
    @DisplayName("CartException")
    class Cart {
        @Test
        void returnsExceptionStatusNotHardcoded400() {
            var ex = new CartException(HttpStatus.NOT_FOUND.toString(), "Cart not found for user.", HttpStatus.NOT_FOUND.value());
            var response = handler.handleCartException(ex, request);

            assertCommon(response, HttpStatus.NOT_FOUND, "Cart not found for user.");
            assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.toString());
            assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("CheckoutException")
    class Checkout {
        @Test
        void returns400ForBadRequestCheckout() {
            var ex = new CheckoutException(HttpStatus.BAD_REQUEST.toString(), "Cannot checkout with empty cart.", HttpStatus.BAD_REQUEST.value());
            var response = handler.checkoutException(ex, request);

            assertCommon(response, HttpStatus.BAD_REQUEST, "Cannot checkout with empty cart.");
            assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void returns404WhenCheckoutNotFound() {
            var ex = new CheckoutException(HttpStatus.NOT_FOUND.toString(), "No active checkout found for user.", HttpStatus.NOT_FOUND.value());
            var response = handler.checkoutException(ex, request);

            assertCommon(response, HttpStatus.NOT_FOUND, "No active checkout found for user.");
        }
    }

    @Nested
    @DisplayName("OrderException")
    class Order {
        @Test
        void returnsExceptionStatus() {
            var ex = new OrderException(HttpStatus.NOT_FOUND.toString(), "Order not found.", HttpStatus.NOT_FOUND.value());
            var response = handler.orderException(ex, request);

            assertCommon(response, HttpStatus.NOT_FOUND, "Order not found.");
        }

        @Test
        void returns400ForInvalidStatusTransition() {
            var ex = new OrderException(HttpStatus.BAD_REQUEST.toString(), "Cannot update cancelled order.", HttpStatus.BAD_REQUEST.value());
            var response = handler.orderException(ex, request);

            assertCommon(response, HttpStatus.BAD_REQUEST, "Cannot update cancelled order.");
        }
    }

    @Nested
    @DisplayName("UserNotFoundException")
    class UserNotFound {
        @Test
        void returnsExceptionStatusNotHardcoded400() {
            var ex = new UserNotFoundException(HttpStatus.NOT_FOUND.toString(), "User not found.", HttpStatus.NOT_FOUND.value());
            var response = handler.userNotFoundExceptionException(ex, request);

            assertCommon(response, HttpStatus.NOT_FOUND, "User not found.");
            assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class Validation {
        @Test
        void returns400WithValidationMessage() {
            var ex = new ValidationException(HttpStatus.BAD_REQUEST.toString(), "Phone number must be 10 digits.", HttpStatus.BAD_REQUEST.value());
            var response = handler.validationException(ex, request);

            assertCommon(response, HttpStatus.BAD_REQUEST, "Phone number must be 10 digits.");
        }
    }

    @Nested
    @DisplayName("InventoryException")
    class Inventory {
        @Test
        void returnsExceptionStatus() {
            var ex = new InventoryException(HttpStatus.CONFLICT.toString(), "Insufficient stock for product.", HttpStatus.CONFLICT.value());
            var response = handler.handleInventoryException(ex, request);

            assertCommon(response, HttpStatus.CONFLICT, "Insufficient stock for product.");
        }
    }

    @Nested
    @DisplayName("MethodNotAllowedException (custom)")
    class CustomMethodNotAllowed {
        @Test
        void returnsExceptionStatus() {
            var ex = new MethodNotAllowedException(HttpStatus.METHOD_NOT_ALLOWED.toString(), "Operation not permitted.", HttpStatus.METHOD_NOT_ALLOWED.value());
            var response = handler.handleMethodNotAllowedException(ex, request);

            assertCommon(response, HttpStatus.METHOD_NOT_ALLOWED, "Operation not permitted.");
        }
    }

    @Nested
    @DisplayName("PaymentException")
    class Payment {
        @Test
        void returnsPaymentRequiredNotHardcoded500() {
            var ex = new PaymentException("PAYMENT_FAILED", "Card declined.", HttpStatus.PAYMENT_REQUIRED.value());
            var response = handler.handlePaymentException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Card declined.");
            assertThat(response.getBody().getTimestamp()).isNotNull();
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        }
    }

    @Nested
    @DisplayName("NullPointerException (custom)")
    class NullPointer {
        @Test
        void returns500WithGenericMessage() {
            var ex = new NullPointerException("NULL_POINTER", "Null reference encountered.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            var response = handler.nullPointerException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo("Null pointer exception occurred");
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        }
    }

    @Nested
    @DisplayName("ArrayIndexOutOfBoundsException (custom)")
    class ArrayOutOfBounds {
        @Test
        void returns500WithGenericMessage() {
            var ex = new ArrayIndexOutOfBoundsException("ARRAY_OUT_OF_BOUNDS", "Index 5 out of bounds for length 3.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            var response = handler.arrayIndexOutOfBoundsException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo("Array out of bounds has occurred");
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        }
    }

    @Nested
    @DisplayName("Catch-all Exception")
    class CatchAll {
        @Test
        void returns500WithGenericMessage() {
            var ex = new java.io.IOException("unexpected failure");
            var response = handler.handleUnexpectedException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        }
    }

    @Nested
    @DisplayName("Correlation ID")
    class CorrelationId {
        @Test
        void propagatesClientSuppliedCorrelationId() {
            when(request.getHeader("X-Correlation-ID")).thenReturn("client-req-123");
            var ex = new ProductException(HttpStatus.BAD_REQUEST.toString(), "Not found.", HttpStatus.BAD_REQUEST.value());

            var response = handler.handleProductException(ex, request);

            assertThat(response.getHeaders().getFirst("X-Correlation-ID")).isEqualTo("client-req-123");
        }

        @Test
        void generatesCorrelationIdWhenNotSupplied() {
            when(request.getHeader("X-Correlation-ID")).thenReturn(null);
            var ex = new ProductException(HttpStatus.BAD_REQUEST.toString(), "Not found.", HttpStatus.BAD_REQUEST.value());

            var response = handler.handleProductException(ex, request);

            assertThat(response.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        }
    }

    // ── Shared assertion ─────────────────────────────────────────────────────

    private void assertCommon(ResponseEntity<GlobalApiErrorResponse> response,
                              HttpStatus expectedStatus, String expectedMessage) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);

        GlobalApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo(expectedMessage);
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getPath()).isEqualTo("/api/v1/test");
    }
}
