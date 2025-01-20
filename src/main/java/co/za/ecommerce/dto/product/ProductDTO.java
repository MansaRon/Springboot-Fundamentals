package co.za.ecommerce.dto.product;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO extends EntityDTO {

    @NonNull
    private String description;

    @NonNull
    private String category;

    @NonNull
    private String imageUrl;

    @PositiveOrZero
    private double price;

    @NonNull
    private String rate;

    @NonNull
    private String title;

    @NotNull
    private int quantity;

    private List<String> images;
}
