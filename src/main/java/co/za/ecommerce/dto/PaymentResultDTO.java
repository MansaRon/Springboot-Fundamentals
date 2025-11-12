package co.za.ecommerce.dto;

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
public class PaymentResultDTO {
    private boolean success;
    private String transactionId;
    private String paymentReference;
    private String paymentStatus;
    private double amountPaid;
    private String errorMessage;
}
