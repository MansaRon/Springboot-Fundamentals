package co.za.ecommerce.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ResourceNotFoundException extends RuntimeException {
    private final String code;
    private final Integer status;

    public ResourceNotFoundException(String code, String message, Integer status) {
        super(message);
        log.error(String.format("Message: %s, Status: %s", message, status));
        this.code = code;
        this.status = status;
    }
}
