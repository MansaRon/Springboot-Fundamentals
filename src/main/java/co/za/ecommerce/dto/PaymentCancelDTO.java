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
public class PaymentCancelDTO extends EntityDTO {
    private boolean cancelled;
    private String message;
    private String paymentRequestId;
}
