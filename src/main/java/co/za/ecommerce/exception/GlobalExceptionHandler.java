package co.za.ecommerce.exception;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
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

    // TODO
    // Create exception for unknown URL's
    // Add exception for handling 415 errors
    private String getPath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return (path != null) ? path : "/UNKNOWN_PATH";
    }
}
