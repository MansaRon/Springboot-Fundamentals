package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CartItems {

    @DBRef
    private Product product;

    /**
     * Number of items inside cart.
     */
    private Integer quantity;

    /**
     * Discount of the cart items.
     */
    private double discount;

    /**
     * Tax of the cart items.
     */
    private double tax;

    /**
     * Price of the product.
     */
    private double productPrice;
}
