package co.za.ecommerce.dto.order;

import java.time.LocalDateTime;

import co.za.ecommerce.dto.base.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultsDTO extends EntityDTO {
    private boolean success;
    private String transactionId;
    private String authorizationCode;
    private String responseCode;
    private String responseMessage;
    private LocalDateTime timestamp;
    private PaymentStatus status;
    private double processedAmount;
    private String receiptNumber;
}
