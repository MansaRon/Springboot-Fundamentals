package co.za.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ClientException extends RuntimeException {
    private final HttpStatus code;

    public ClientException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }
}
