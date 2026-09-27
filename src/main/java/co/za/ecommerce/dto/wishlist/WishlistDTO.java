package co.za.ecommerce.dto.wishlist;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO extends EntityDTO {

    @NotBlank
    private String userID;

    @NotBlank
    private String productID;

    private ProductDTO productDTO;
}
