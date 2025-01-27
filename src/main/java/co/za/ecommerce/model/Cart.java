package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "cart")
public class Cart extends Entity {

    /**
     * The user who owns the cart.
     */
    @DBRef
    private User user;

    /**
     * Items in the cart.
     */
    private List<CartItems> cartItems;

    /**
     * Discount of the cart items.
     */
    private double discount;

    /**
     * Tax of the cart items.
     */
    private double tax;

    /**
     * Total price of all items in the cart.
     */
    private double totalPrice;

    public void updateTotal() {
        this.totalPrice = cartItems
                .stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
}
