package co.za.ecommerce.dto.checkout;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.dto.order.AddressDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
    private UserDTO user;
    @JsonIgnore
    private ObjectId cartId;
    private List<CartItemsDTO> items;
    private double subtotal;
    private double discount;
    private double tax;
    private double totalAmount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Valid
    @NotNull(message = "Shipping address is required")
    private AddressDTO shippingAddress;

    @Valid
    @NotNull(message = "Billing address is required")
    private AddressDTO billingAddress;

    @NotNull(message = "Shipping method is required")
    private DeliverMethod shippingMethod;

    private LocalDate estimatedDeliveryDate;
    private String status;
}
