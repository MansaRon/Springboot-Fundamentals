package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "order")
public class Order extends Entity {

    private User customerInfo;
    private List<OrderItems> orderItems;
    private PaymentDetails paymentDetails;
    private Address shippingAddress;
    private Address billingAddress;
    private OrderStatus orderStatus;
    private String shippingMethod;
    private LocalDateTime estimatedDeliveryDate;
    private double subtotal;
    private double discount;
    private double tax;
    private double totalAmount;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private double shippingCost;
}
