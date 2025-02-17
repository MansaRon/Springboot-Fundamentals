package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderItems extends Entity {
    private ObjectId productId;
    private int quantity;
    private double totalPrice;
    private String imageUrl;
}

// add order item
// get all order items
// get order item by userid
