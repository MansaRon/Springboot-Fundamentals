package co.za.ecommerce.dto;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
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
    private PaymentStatus paymentStatus;
    private double amountProcessed;
    private String paymentMethod;
    private String failureReason;
    private String message;
}
