package co.za.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class OTPException extends RuntimeException {
    private HttpStatus code;

    public OTPException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }
}
