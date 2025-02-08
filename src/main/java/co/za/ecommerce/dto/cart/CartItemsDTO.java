package co.za.ecommerce.dto.cart;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsDTO extends EntityDTO {
    private ObjectId cartItemId;
    private ProductDTO productDTO;
    private Integer quantity;
    private double discount;
    private double tax;
    private double productPrice;
}
