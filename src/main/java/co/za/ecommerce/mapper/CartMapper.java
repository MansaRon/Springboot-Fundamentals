package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Image;
import co.za.ecommerce.model.Product;

import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

public class CartMapper {

    public static CartDTO toDTO(Cart cart) {
        if (cart == null) return null;
        CartDTO dto = new CartDTO();
        dto.setTotalPrice(cart.getTotalPrice());
        if (cart.getCartItems() != null) {
            dto.setCartItems(cart.getCartItems()
                    .stream()
                    .map(CartMapper::toCartItemsDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    static CartItemsDTO toCartItemsDTO(CartItems item) {
        if (item == null) return null;
        CartItemsDTO dto = new CartItemsDTO();
        dto.setQuantity(item.getQuantity());
        dto.setDiscount(item.getDiscount());
        dto.setTax(item.getTax());
        dto.setProductPrice(item.getProductPrice());
        dto.setProductDTO(toProductDTO(item.getProduct()));
        return dto;
    }

    static ProductDTO toProductDTO(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId() != null ? product.getId().toHexString() : null);
        dto.setCreatedAt(now());
        dto.setUpdatedAt(now());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrice(product.getPrice());
        dto.setRate(product.getRate());
        dto.setQuantity(product.getQuantity());

        dto.setImages(
                product.getImages() != null ?
                product.getImages().stream().map(Image::getFileName).collect(Collectors.toList()) :
                null);

        return dto;
    }

    public static Cart fromDTO(CartDTO dto) {
        if (dto == null) return null;
        Cart cart = new Cart();
        cart.setUpdatedAt(now());
        cart.setTotalPrice(dto.getTotalPrice());
        if (dto.getCartItems() != null) {
            cart.setCartItems(dto.getCartItems().stream()
                    .map(CartMapper::fromCartItemsDTO)
                    .collect(Collectors.toList()));
        }
        return cart;
    }

    private static CartItems fromCartItemsDTO(CartItemsDTO dto) {
        if (dto == null) return null;
        CartItems item = new CartItems();
        item.setQuantity(dto.getQuantity());
        item.setDiscount(dto.getDiscount());
        item.setTax(dto.getTax());
        item.setProductPrice(dto.getProductPrice());
        item.setProduct(fromProductDTO(dto.getProductDTO()));
        return item;
    }

    private static Product fromProductDTO(ProductDTO dto) {
        if (dto == null) return null;
        Product product = new Product();
        if (dto.getId() != null) {
            product.setId(new org.bson.types.ObjectId(dto.getId()));
        }
        product.setCreatedAt(dto.getCreatedAt());
        product.setUpdatedAt(now());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());
        product.setRate(dto.getRate());
        product.setQuantity(dto.getQuantity());
        product.setImages(null);
        return product;
    }
}
