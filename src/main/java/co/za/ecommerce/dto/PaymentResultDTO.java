package co.za.ecommerce.dto;

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
public class PaymentResultDTO extends EntityDTO {
    private boolean success;
    private String transactionId;
    private String paymentStatus;
    private double amountProcessed;
    private String paymentMethod;
    private String failureReason;
    private String message;
}
