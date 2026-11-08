package co.za.ecommerce.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
public class EmailException extends RuntimeException {
    private final String code;
    private final Integer status;

    public EmailException(String code, String message, Integer status, Throwable cause) {
        super(message, cause);
        log.error(String.format("Message: %s, Status: %s", message, status));
        this.code = code;
        this.status = status;
    }

    /**
     * Convenience constructor defaulting to a 503 (mail delivery is a downstream dependency).
     */
    public EmailException(String message, Throwable cause) {
        this(HttpStatus.SERVICE_UNAVAILABLE.toString(), message, HttpStatus.SERVICE_UNAVAILABLE.value(), cause);
    }
}
