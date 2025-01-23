package co.za.ecommerce.dto.wishlist;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.model.Product;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO extends EntityDTO {

    @NotNull
    private ObjectId userID;

    @NotNull
    private ObjectId productID;

    @NotNull
    private ProductDTO productDTO;
}
