package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.UserNotFoundException;
import co.za.ecommerce.mapper.CartMapper;
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
    private UserRepository userRepository;

    @Override
    public CartDTO addProductToCart(ObjectId userId, ObjectId productId, int quantity) {
        if (productId.toString().length() != 24) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid product ID format: " + productId,
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (userId.toString().length() != 24) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid user ID format: " + userId,
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()
                ));

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

        Optional<CartItems> existingCartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItems item = existingCartItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setProductPrice(item.getProductPrice() + (product.getPrice() * quantity));
        } else {
            CartItems newCartItem = new CartItems();
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setDiscount(0);
            newCartItem.setTax(0);
            newCartItem.setProductPrice(product.getPrice() * quantity);
            cart.getCartItems().add(newCartItem);
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        cart.updateTotal();

        Cart updatedCart = cartRepository.save(cart);

        return CartMapper.toDTO(updatedCart);
    }

    @Override
    public CartDTO getUserCartWithItems(ObjectId userId) {
        if (userId.toString().length() != 24) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid user ID format: " + userId,
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        return CartMapper.toDTO(cart);
    }

    // Updates single item inside the cart
    @Override
    public CartDTO updateProductInCart(ObjectId userId, ObjectId productId, int newQuantity) {
        if (productId.toString().length() != 24) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid product ID format: " + productId,
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (userId.toString().length() != 24) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid user ID format: " + userId,
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()));

        Optional<CartItems> existingCartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItems cartItem = existingCartItem.get();

            if (newQuantity <= 0) {
                cart.getCartItems().remove(cartItem);
            } else {
                if (newQuantity > product.getQuantity()) {
                    throw new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Only " + product.getQuantity() + " units available for " + product.getTitle(),
                            HttpStatus.BAD_REQUEST.value()
                    );
                }

                int previousQuantity = cartItem.getQuantity();
                cartItem.setQuantity(newQuantity);
                cartItem.setProductPrice(newQuantity * product.getPrice());

                product.setQuantity(product.getQuantity() - (newQuantity - previousQuantity));
                productRepository.save(product);
            }

            cart.updateTotal();
            cartRepository.save(cart);

            return CartMapper.toDTO(cart);
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
        if (productId.toString().length() != 24) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid product ID format: " + productId,
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product not found",
                        HttpStatus.BAD_REQUEST.value()));

        Optional<CartItems> existingCartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItems cartItem = existingCartItem.get();

            product.setQuantity(product.getQuantity() + cartItem.getQuantity());
            productRepository.save(product);

            cart.getCartItems().remove(cartItem);

            cart.updateTotal();

            Cart updatedCart = cartRepository.save(cart);

            return CartMapper.toDTO(updatedCart);
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
