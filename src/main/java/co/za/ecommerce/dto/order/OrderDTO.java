package co.za.ecommerce.dto.order;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.model.checkout.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO extends EntityDTO {
    private CustomerDTO customerInfo;
    private List<OrderItemsDTO> orderItems;
    private PaymentDTO paymentDetails;
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;
    private String orderStatus;
    private String shippingMethod;
    private LocalDateTime estimatedDeliveryDate;
    private double subtotal;
    private double discount;
    private double tax;
    private double totalAmount;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private double shippingCost;
    private String orderNumber;
}
