package co.za.ecommerce.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "products")
public class Product extends Entity {

    /**
     * Short description defining the instance.
     */
    @NonNull
    private String description;

    /**
     * Category for the product (Eg. Jewellery, Men's clothing etc...)
     */
    @NonNull
    private String category;

    /**
     * URL of the image.
     */
    @NonNull
    private String imageUrl;

    /**
     * Price for the product.
     */
    @PositiveOrZero
    private double price;

    /**
     * Rating of the product.
     */
    @NonNull
    private String rate;

    /**
     * Title of the product.
     */
    @NonNull
    private String title;

    /**
     * Number of items of product available.
     */
    @NotNull
    @Size(min = 1, max = 20)
    private int quantity;
}
