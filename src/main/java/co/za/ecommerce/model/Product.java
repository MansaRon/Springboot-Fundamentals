package co.za.ecommerce.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "products")
public class Product extends Entity {

    /**
     * Short description defining the instance.
     */
    @NotNull
    private String description;

    /**
     * Category for the product (Eg. Jewellery, Men's clothing etc...)
     */
    @NotNull
    private String category;

    /**
     * URL of the image.
     */
    private List<String> imageUrls;

    /**
     * Price for the product.
     */
    @PositiveOrZero
    private double price;

    /**
     * Rating of the product.
     */
    @NotNull
    private String rate;

    /**
     * Title of the product.
     */
    @NotNull
    private String title;

    /**
     * Number of items of product available.
     */
    @NotNull
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    private int quantity;

    /**
     * User ratings/reviews of the product
     */
    private List<Rating> reviews;
}
