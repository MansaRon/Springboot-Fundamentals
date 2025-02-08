package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.UserNotFoundException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
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
    public CartDTO getUserCartWithItems(ObjectId userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        return objectMapper.mapObject().map(cart, CartDTO.class);
    }

    @Override
    public CartDTO updateProductInCart(ObjectId userId, ObjectId productId, int newQuantity) {
        // Find the user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()));

        // Find the cart item
        Optional<CartItems> existingCartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItems cartItem = existingCartItem.get();

            // Check if the new quantity is valid
            if (newQuantity <= 0) {
                // Remove the item from cart if quantity is 0
                cart.getCartItems().remove(cartItem);
            } else {
                // Check product stock availability
                if (newQuantity > product.getQuantity()) {
                    throw new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Only " + product.getQuantity() + " units available for " + product.getTitle(),
                            HttpStatus.BAD_REQUEST.value()
                    );
                }

                // Update the cart item with new quantity and price
                int previousQuantity = cartItem.getQuantity();
                cartItem.setQuantity(newQuantity);
                cartItem.setProductPrice(newQuantity * product.getPrice());

                // Update product stock (if quantity changed)
                product.setQuantity(product.getQuantity() - (newQuantity - previousQuantity));
                productRepository.save(product);
            }

            // Recalculate the total cart price
            cart.updateTotal();
            cartRepository.save(cart);

            // Map and return updated cart
            return objectMapper.mapObject().map(cart, CartDTO.class);
        } else {
            throw new CartException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Product not found in the cart",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public CartDTO deleteProductFromCart(ObjectId userId, ObjectId productId) {
        // Find the user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()));

        // Find the cart item
        Optional<CartItems> existingCartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItems cartItem = existingCartItem.get();

            // Restore product stock
            product.setQuantity(product.getQuantity() + cartItem.getQuantity());
            productRepository.save(product);

            // Remove item from cart
            cart.getCartItems().remove(cartItem);

            // Recalculate the cart total price
            cart.updateTotal();

            // Save the updated cart
            Cart updatedCart = cartRepository.save(cart);

            // Map to DTO and return
            return objectMapper.mapObject().map(updatedCart, CartDTO.class);
        } else {
            throw new CartException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Product not found in the cart",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private Cart createNewCartForUser(ObjectId userID) {
        Cart newCart = new Cart();
        newCart.setUser(userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(
                HttpStatus.BAD_REQUEST.toString(),
                "User not found",
                HttpStatus.BAD_REQUEST.value())));
        newCart.setCartItems(new ArrayList<>());
        newCart.setCreatedAt(now());
        newCart.setUpdatedAt(now());
        return cartRepository.save(newCart);
    }
}
