package co.za.ecommerce.dto.checkout;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.PaymentMethod;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutStatusDTO extends EntityDTO {
    private String checkoutId;
    private CheckoutStatus status;
    private PaymentStatus paymentStatus;
    private double totalAmount;
    private PaymentMethod paymentMethod;
}
