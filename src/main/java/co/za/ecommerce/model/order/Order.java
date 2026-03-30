package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "order")
public class Order extends Entity {
    /**
     * The user whom the order belongs to.
     */
    private User customerInfo;

    /**
     * The list of ordered items.
     */
    private List<OrderItems> orderItems;

    /**
     * Payment details of the order.
     */
    private PaymentDetails paymentDetails;

    /**
     * Shipping address of the order.
     */
    private Address shippingAddress;

    /**
     * Billing address of the order.
     */
    private Address billingAddress;

    /**
     * The status of the order.
     */
    private OrderStatus orderStatus;

    /**
     * Shipping method of the order.
     */
    private String shippingMethod;

    /**
     * The estimated delivery date of the order.
     */
    private LocalDateTime estimatedDeliveryDate;

    /**
     * The date the order is shipped.
     */
    private LocalDateTime shippedDate;

    /**
     * Subtotal of the order.
     */
    private double subtotal;

    /**
     * Discount of the order.
     */
    private double discount;

    /**
     * Tax of the order.
     */
    private double tax;

    /**
     * Total amount of the order.
     */
    private double totalAmount;

    /**
     * The transaction ID of the order.
     */
    private String transactionId;

    /**
     * Payment type for the order.
     */
    private PaymentMethod paymentMethod;

    /**
     * Shipping costs of the order.
     */
    private double shippingCost;

    /**
     * Order number for the order.
     */
    private String orderNumber;

    /**
     * Additional notes for the order.
     */
    private String notes;

    /**
     * History status of the order.
     */
    private List<OrderStatusHistory> statusHistory;
}
