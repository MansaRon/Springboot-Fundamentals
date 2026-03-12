package co.za.ecommerce.dto.cart;

import co.za.ecommerce.dto.product.ProductDTO;
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
public class CartItemsDTO {
    private ProductDTO productDTO;
    private Integer quantity;
    private double discount;
    private double tax;
    private double productPrice;
}
