package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import co.za.ecommerce.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderItems extends Entity {
    @DBRef
    private Product product;

    /**
     * Snapshot of the product's title at order time. Kept so the order line stays accurate
     * even if the product is later renamed or deleted (the {@code @DBRef} above resolves to
     * null once a product is removed).
     */
    private String productTitle;

    /**
     * Snapshot of the product's images at order time (same rationale as {@link #productTitle}).
     */
    private List<String> imageUrls;

    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double discount;
    private double tax;
    private String imageUrl;
}