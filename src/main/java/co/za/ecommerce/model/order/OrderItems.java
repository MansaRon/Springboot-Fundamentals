package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import co.za.ecommerce.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderItems extends Entity {
    @DBRef
    private Product product;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double discount;
    private double tax;
    private String imageUrl;
}

// add order item
// get all order items
// get order item by userid
