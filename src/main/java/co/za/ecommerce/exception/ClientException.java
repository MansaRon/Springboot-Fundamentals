package co.za.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ClientException extends RuntimeException {
    private HttpStatus code;

    public ClientException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }
}
