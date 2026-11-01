package co.za.ecommerce.dto.cart;

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
public class CartDTO {
    private List<CartItemsDTO> cartItems;
    // TODO add in the tax variable here so the overall object reflects what's on the UI
    private double totalPrice;
}
