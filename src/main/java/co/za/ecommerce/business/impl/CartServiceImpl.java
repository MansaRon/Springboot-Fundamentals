package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.UserNotFoundException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.CartItemRepository;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartItemRepository cartItemRepository;
    private ObjectMapper objectMapper;
    private UserRepository userRepository;

    @Override
    public CartDTO addProductToCart(ObjectId userId, ObjectId productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()
                ));

        // Check product availability
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

        // Check if the product is already in the cart
        Optional<CartItems> existingCartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // If product exists in the cart, update quantity and price
            CartItems item = existingCartItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setProductPrice(item.getProductPrice() + (product.getPrice() * quantity));
        } else {
            // If product is not in the cart, add as new item
            CartItems newCartItem = new CartItems();
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setDiscount(0);  // Update with discount logic if applicable
            newCartItem.setTax(0);       // Update with tax logic if applicable
            newCartItem.setProductPrice(product.getPrice() * quantity);
            cart.getCartItems().add(newCartItem);
        }

        // Deduct the purchased quantity from the product stock
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        // Update total price in the cart
        cart.updateTotal();

        // Save the cart with the new items
        Cart updatedCart = cartRepository.save(cart);

        // Map to DTO and return
        return objectMapper.mapObject().map(updatedCart, CartDTO.class);
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

    private Cart createNewCartForUser(ObjectId userID) {
        Cart newCart = new Cart();
        newCart.setUser(userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(
                HttpStatus.BAD_REQUEST.toString(),
                "User not found",
                HttpStatus.BAD_REQUEST.value())));
        newCart.setCreatedAt(now());
        newCart.setUpdatedAt(now());
        return cartRepository.save(newCart);
    }
}
