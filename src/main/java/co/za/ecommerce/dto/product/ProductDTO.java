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

    @NotNull
    private String description;

    @NotNull
    private String category;

    @PositiveOrZero
    private double price;

    @NotNull
    private String rate;

    @NotNull
    private String title;

    @NotNull
    private int quantity;

    private List<String> imageUrls;
}
