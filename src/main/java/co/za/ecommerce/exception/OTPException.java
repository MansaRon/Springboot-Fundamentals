package co.za.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OTPException extends RuntimeException {
    private final HttpStatus code;

    public OTPException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }
}
