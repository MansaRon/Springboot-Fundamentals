package co.za.ecommerce.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "wishlist")
public class Wishlist extends Entity {

    /**
     * User ID for the wishlist item associated with user.
     */
    @NotNull
    private ObjectId userID;

    /**
     * Product ID for the wishlist item associated with user.
     */
    @NotNull
    private ObjectId productID;

    /**
     * Associated product.
     */
    @DBRef
    @NotNull
    private Product product;
}
