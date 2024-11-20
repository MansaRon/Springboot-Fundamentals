package co.za.ecommerce.business;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public abstract class GlobalApiResponse {
    private String status;
    private Integer statusCode;
    private String message;
    private Instant timestamp;
}
