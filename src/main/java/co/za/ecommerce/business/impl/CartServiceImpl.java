package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.CartItemRepository;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartItemRepository cartItemRepository;
    private ObjectMapper objectMapper;

    @Override
    public CartDTO addProductToCart(ObjectId cartId, ObjectId productId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found",
                        HttpStatus.BAD_REQUEST.value()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()
                ));

        CartItems cartItem = cartItemRepository
                .findCartItemsByProductIdAndCartId(cartId, productId);

        if (cartItem != null) {
            throw new CartException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cart not found",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (product.getQuantity() == 0) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    product.getTitle() + " is not available.",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (product.getQuantity() < quantity) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Please make an order of the " + product.getTitle() + " less than or equal to the " + product.getQuantity() + ".",
                    HttpStatus.BAD_REQUEST.value());
        }

        CartItems cartItems = new CartItems();
        cartItems.setProduct(product);
        cartItems.setQuantity(quantity);
        // TODO
        //cartItems.setDiscount(product.);
        cartItems.setProductPrice(product.getPrice());
        cartItemRepository.save(cartItems);
        product.setQuantity(product.getQuantity() - quantity);
        cart.setTotalPrice(cart.getTotalPrice() + product.getPrice());

        CartDTO cartDTO = objectMapper
                .mapObject()
                .map(cart, CartDTO.class);
        
        List<ProductDTO> productDTOs = cart.getCartItems()
                .stream().map(p -> objectMapper
                        .mapObject()
                        .map(p.getProduct(), ProductDTO.class))
                .toList();
        return cartDTO;
    }

    @Override
    public List<Cart> getAllCarts() {
        return List.of();
    }

    @Override
    public CartDTO getCart(ObjectId userId, ObjectId cartId) {
        return null;
    }

    @Override
    public CartDTO updateProductInCart(ObjectId cartId, ObjectId productId) {
        return null;
    }

    @Override
    public CartDTO updateProductQuantityInCart(ObjectId cartId, ObjectId productId, Integer quantity) {
        return null;
    }

    @Override
    public void deleteProductFromCart(ObjectId cartId, ObjectId productId) {

    }
}
