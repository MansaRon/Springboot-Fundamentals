package co.za.ecommerce.exception;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);
        when(request.getAttribute(PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/api/v1/test");
    }

    @Test
    @DisplayName("shouldReturn400WithCorrectBodyWhenClientExceptionThrown")
    void handleClientException() {
        ClientException ex = new ClientException(HttpStatus.BAD_REQUEST, "Email already exists.");

        ResponseEntity<GlobalApiErrorResponse> response = handler.handleClientException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Email already exists.");
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("shouldReturn400WithCorrectBodyWhenOTPExceptionThrown")
    void handleOTPException() {
        OTPException ex = new OTPException(HttpStatus.BAD_REQUEST, "Invalid OTP expired.");

        ResponseEntity<GlobalApiErrorResponse> response = handler.handleOTPException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Invalid OTP expired.");
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("shouldReturn400WithProductCodeAndStatusInBody")
    void handleProductException() {
        ProductException ex = new ProductException(HttpStatus.BAD_REQUEST.toString(), "Product not found.", HttpStatus.BAD_REQUEST.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.handleProductException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Product not found.");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.toString());
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("shouldReturn400WithCartCodeAndStatusInBody")
    void handleWishlistException() {
        CartException ex = new CartException(HttpStatus.NOT_FOUND.toString(), "Cart not found for user.", HttpStatus.NOT_FOUND.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.handleCartException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Cart not found for user.");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.toString());
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("shouldReturn400WithCheckoutCodeAndStatusInBody")
    void handleCartException() {
        CheckoutException ex = new CheckoutException(HttpStatus.BAD_REQUEST.toString(), "Cannot checkout with empty cart.", HttpStatus.BAD_REQUEST.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.checkoutException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Cannot checkout with empty cart.");
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void resourceNotFoundExceptionException() {}

    @Test
    @DisplayName("shouldReturn400WithUserNotFoundMessageInBody")
    void userNotFoundExceptionException() {
        UserNotFoundException ex = new UserNotFoundException(HttpStatus.NOT_FOUND.toString(), "User not found.", HttpStatus.NOT_FOUND.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.userNotFoundExceptionException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "User not found.");
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("shouldReturn400WithCheckoutCodeAndStatusInBody")
    void checkoutException() {
        CheckoutException ex = new CheckoutException(HttpStatus.BAD_REQUEST.toString(), "Cannot checkout with empty cart.", HttpStatus.BAD_REQUEST.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.checkoutException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Cannot checkout with empty cart.");
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("shouldReturn400WithValidationMessageInBody")
    void validationException() {
        ValidationException ex = new ValidationException(HttpStatus.BAD_REQUEST.toString(), "Phone number must be 10 digits.", HttpStatus.BAD_REQUEST.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.validationException(ex, request);

        assertCommonFields(response, HttpStatus.BAD_REQUEST, "Phone number must be 10 digits.");
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("handleNullPointerException")
    void nullPointerException() {
        NullPointerException ex = new NullPointerException("NULL_POINTER", "Null reference encountered.", HttpStatus.INTERNAL_SERVER_ERROR.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.nullPointerException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Null pointer exception occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    @DisplayName("shouldReturn500WithArrayOutOfBoundsMessageInBody")
    void arrayIndexOutOfBoundsException() {
        ArrayIndexOutOfBoundsException ex = new ArrayIndexOutOfBoundsException("ARRAY_OUT_OF_BOUNDS", "Index 5 out of bounds for length 3.", HttpStatus.INTERNAL_SERVER_ERROR.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.arrayIndexOutOfBoundsException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Array out of bounds has occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    @DisplayName("shouldReturn500WithPaymentFailedMessageInBody")
    void handlePaymentException() {
        PaymentException ex = new PaymentException("PAYMENT_FAILED", "Card declined.", HttpStatus.PAYMENT_REQUIRED.value());

        ResponseEntity<GlobalApiErrorResponse> response = handler.handlePaymentException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Card declined.");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    private void assertCommonFields(ResponseEntity<GlobalApiErrorResponse> response, HttpStatus expectedHttpStatus, String expectedMessage) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);

        GlobalApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo(expectedMessage);
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getPath()).isEqualTo("/api/v1/test");
    }
}