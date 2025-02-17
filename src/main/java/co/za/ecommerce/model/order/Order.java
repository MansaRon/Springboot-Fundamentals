package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "order")
public class Order extends Entity {

    private CustomerInfo customerInfo;
    private List<OrderItems> orderItems;
    private PaymentDetails paymentDetails;
    private Address shippingAddress;
    private Address billingAddress;
    private OrderStatus orderStatus;

}
