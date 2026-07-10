package co.za.ecommerce.dto.product;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO extends EntityDTO {

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @NotBlank(message = "Rate is required")
    private String rate;

    @NotBlank(message = "Title is required")
    private String title;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    private List<String> imageUrls;

    private List<RatingDTO> reviews;
}
