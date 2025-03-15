package co.za.ecommerce.dto.order;

import co.za.ecommerce.dto.base.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO extends EntityDTO {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private double amount;
    private String currency;
    private String merchantId;
    private String description;
    private String referenceId;
    private String paymentMethod;
}
