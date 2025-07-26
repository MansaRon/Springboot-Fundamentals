package co.za.ecommerce.dto.cart;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.model.CartItems;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO extends EntityDTO {
    private List<CartItemsDTO> cartItems;
    private double totalPrice = 0.0;
}
