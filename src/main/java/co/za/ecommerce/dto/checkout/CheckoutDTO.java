package co.za.ecommerce.dto.checkout;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.dto.order.AddressDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO extends EntityDTO {
    private UserDTO userDTO;
    private ObjectId cartId;
    private List<CartItemsDTO> items;
    private double subtotal;
    private double discount;
    private double tax;
    private double totalAmount;
    private PaymentMethod paymentMethod;
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;
    private DeliverMethod shippingMethod;
    private LocalDate estimatedDeliveryDate;
    private String status;
}
