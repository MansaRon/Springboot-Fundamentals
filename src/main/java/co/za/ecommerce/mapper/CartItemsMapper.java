package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.model.CartItems;

import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.mapper.CartMapper.toProductDTO;

public class CartItemsMapper {

    public static CartItemsDTO toDTO(CartItems cartItems) {
        if (cartItems == null) return null;

        CartItemsDTO dto = new CartItemsDTO();
        dto.setProductDTO(toProductDTO(cartItems.getProduct()));
        dto.setQuantity(cartItems.getQuantity());
        dto.setDiscount(cartItems.getDiscount());
        dto.setTax(cartItems.getTax());
        dto.setProductPrice(cartItems.getProductPrice());
        return dto;
    }

    public List<CartItemsDTO> toDTOList(List<CartItems> cartItems) {
        if (cartItems == null) return null;
        return cartItems.stream().map(CartItemsMapper::toDTO).collect(Collectors.toList());
    }
}
