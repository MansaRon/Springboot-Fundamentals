package co.za.ecommerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class GlobalApiErrorResponse {
    private String status;
    private Integer statusCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}
