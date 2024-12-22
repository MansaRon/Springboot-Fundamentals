package co.za.ecommerce.exception;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ClientException.class})
    public ResponseEntity<GlobalApiErrorResponse> handleClientException(
            final ClientException clientException, final HttpServletRequest httpStatus) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GlobalApiErrorResponse.builder()
                                .path(clientException.getMessage())
                                .status(HttpStatus.BAD_REQUEST.toString())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .path(getPath(httpStatus))
                                .message(clientException.getMessage())
                                .timestamp(Instant.now())
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
                                .timestamp(Instant.now())
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
                        .timestamp(Instant.now())
                        .build()
                );
    }


    private String getPath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return (path != null) ? path : "/UNKNOWN_PATH";
    }
}
