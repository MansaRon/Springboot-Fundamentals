package co.za.ecommerce.model.checkout;

import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Entity;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.Address;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "checkout")
public class Checkout extends Entity {

    /**
     * The user who is checking out.
     */
    @DBRef
    @NotNull
    private User user;

    /**
     * The cart being checked out.
     */
    @DBRef
    @NotNull
    private Cart cart;

    /**
     * The list of products being purchased.
     */
    @NotNull
    private List<CartItems> items;

    /**
     * The total price of the order before tax and discounts.
     */
    @PositiveOrZero
    private double subtotal;

    /**
     * Discount applied to the order.
     */
    @PositiveOrZero
    private double discount;

    /**
     * Tax amount applied to the order.
     */
    @PositiveOrZero
    private double tax;

    /**
     * The final total amount to be paid.
     */
    @PositiveOrZero
    private double totalAmount;

    /**
     * The selected payment method (e.g., Credit Card, PayPal, etc.).
     */
    @DBRef
    @NotNull
    private PaymentMethod paymentMethod;

    /**
     * The shipping address for the order.
     */
    @NotNull
    private Address shippingAddress;

    /**
     * The billing address for the order (if different from shipping).
     */
    @NotNull
    private Address billingAddress;

    /**
     * The shipping method (e.g., Standard, Express).
     */
    @NotNull
    private String shippingMethod;

    /**
     * The estimated delivery date.
     */
    private LocalDate estimatedDeliveryDate;

    /**
     * The current checkout status (e.g., PENDING, COMPLETED, FAILED).
     */
    private CheckoutStatus status;
}
