package co.za.ecommerce.exception;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.utils.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static co.za.ecommerce.utils.DateUtil.now;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ClientException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleClientException(
            final ClientException clientException,
            final HttpServletRequest httpStatus) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GlobalApiErrorResponse.builder()
                                .path(clientException.getMessage())
                                .status(HttpStatus.BAD_REQUEST.toString())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .path(getPath(httpStatus))
                                .message(clientException.getMessage())
                                .timestamp(now())
                                .build()
                );
    }

    @ExceptionHandler({OTPException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleOTPException(
            final OTPException otpException,
            final HttpServletRequest httpStatus) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GlobalApiErrorResponse.builder()
                                .path(otpException.getMessage())
                                .status(HttpStatus.BAD_REQUEST.toString())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .path(getPath(httpStatus))
                                .message(otpException.getMessage())
                                .timestamp(now())
                                .build()
                );
    }

    @ExceptionHandler({ProductException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleProductException(
            final ProductException productException,
            final HttpServletRequest httpStatus) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpStatus))
                        .status(productException.getCode())
                        .statusCode(productException.getStatus())
                        .message(productException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({WishlistException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleWishlistException(
            final WishlistException wishlistException,
            final HttpServletRequest httpStatus
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpStatus))
                        .status(wishlistException.getCode())
                        .statusCode(wishlistException.getStatus())
                        .message(wishlistException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({CartException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleCartException(
            final CartException cartException,
            final HttpServletRequest httpStatus
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpStatus))
                        .status(cartException.getCode())
                        .statusCode(cartException.getStatus())
                        .message(cartException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<GlobalApiErrorResponse> resourceNotFoundExceptionException(
            final ResourceNotFoundException resourceNotFoundException,
            final HttpServletRequest httpStatus
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpStatus))
                        .status(resourceNotFoundException.getCode())
                        .statusCode(resourceNotFoundException.getStatus())
                        .message(resourceNotFoundException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<GlobalApiErrorResponse> userNotFoundExceptionException(
            final UserNotFoundException userNotFoundException,
            final HttpServletRequest httpStatus
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpStatus))
                        .status(userNotFoundException.getCode())
                        .statusCode(userNotFoundException.getStatus())
                        .message(userNotFoundException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({CheckoutException.class})
    public ResponseEntity<GlobalApiErrorResponse> checkoutException(
            final CheckoutException checkoutException,
            final HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpServletRequest))
                        .status(checkoutException.getCode())
                        .statusCode(checkoutException.getStatus())
                        .message(checkoutException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({OrderException.class})
    public ResponseEntity<GlobalApiErrorResponse> orderException(
            final OrderException orderException,
            final HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpServletRequest))
                        .status(orderException.getCode())
                        .statusCode(orderException.getStatus())
                        .message(orderException.getMessage())
                        .timestamp(DateUtil.now())
                        .build());
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<GlobalApiErrorResponse> validationException(
            final ValidationException validationException,
            final HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpServletRequest))
                        .status(validationException.getCode())
                        .statusCode(validationException.getStatus())
                        .message(validationException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<GlobalApiErrorResponse> nullPointerException(final NullPointerException npe, final HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpServletRequest))
                        .status(npe.getMessage())
                        .statusCode(npe.getStatus())
                        .message("Null pointer exception occurred")
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResponseEntity<GlobalApiErrorResponse> arrayIndexOutOfBoundsException(final ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException, final HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpServletRequest))
                        .status(arrayIndexOutOfBoundsException.getMessage())
                        .statusCode(arrayIndexOutOfBoundsException.getStatus())
                        .message("Array out of bounds has occurred")
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<GlobalApiErrorResponse> handlePaymentException(
            final PaymentException paymentException,
            final HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(httpServletRequest))
                        .status(paymentException.getMessage())
                        .statusCode(paymentException.getStatus())
                        .message(paymentException.getMessage())
                        .timestamp(now())
                        .build()
                );
    }

    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleAccessDeniedException(
            final org.springframework.security.access.AccessDeniedException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(request))
                        .status(HttpStatus.FORBIDDEN.toString())
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .message("Access denied. You do not have permission to access this resource.")
                        .timestamp(DateUtil.now())
                        .build()
                );
    }

    @ExceptionHandler({org.springframework.security.core.AuthenticationException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleAuthenticationException(
            final org.springframework.security.core.AuthenticationException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(request))
                        .status(HttpStatus.UNAUTHORIZED.toString())
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .message("Authentication required. Please login to access this resource.")
                        .timestamp(DateUtil.now())
                        .build()
                );
    }

    // 415 errors
    public ResponseEntity<GlobalApiErrorResponse> handleHttpMediaTypeNotSupportedException(
            final org.springframework.web.HttpMediaTypeNotSupportedException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(request))
                        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.toString())
                        .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                        .message("Unsupported media type: " + ex.getContentType()
                                + ". Supported types are: " + ex.getSupportedMediaTypes())
                        .timestamp(DateUtil.now())
                        .build()
                );
    }

    // 405 errors
    public ResponseEntity<GlobalApiErrorResponse> handleMethodNotSupportedException(
            final org.springframework.web.HttpRequestMethodNotSupportedException ex,
            final HttpServletRequest request) {
        String message = "HTTP method " + ex.getMethod() + " is not supported for this endpoint. Supported methods are: " + ex.getSupportedHttpMethods();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(request))
                        .status(HttpStatus.METHOD_NOT_ALLOWED.toString())
                        .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value())
                        .message(message)
                        .timestamp(DateUtil.now())
                        .build()
                );
    }

    // 400 error
    public ResponseEntity<GlobalApiErrorResponse> handleMethodArgumentNotValidException(
            final org.springframework.web.bind.MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiErrorResponse.builder()
                        .path(getPath(request))
                        .status(HttpStatus.BAD_REQUEST.toString())
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .timestamp(DateUtil.now())
                        .build()
                );
    }

    private String getPath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return (path != null) ? path : "/UNKNOWN_PATH";
    }
}
