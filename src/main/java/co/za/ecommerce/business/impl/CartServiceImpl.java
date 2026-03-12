package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.UserNotFoundException;
import co.za.ecommerce.mapper.CartMapper;
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
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        Product product = findProductById(productId);

        validateProductStock(product, quantity);

        Optional<CartItems> existingCartItem = findCartItem(cart, productId);

        if (existingCartItem.isPresent()) {
            CartItems item = existingCartItem.get();
            validateProductStock(product, item.getQuantity() + quantity);
            item.setQuantity(item.getQuantity() + quantity);
            item.setProductPrice(item.getProductPrice() + (product.getPrice() * quantity));
        } else {
            CartItems newCartItem = CartItems.builder()
                    .product(product)
                    .quantity(quantity)
                    .discount(0)
                    .tax(0)
                    .productPrice(product.getPrice() * quantity)
                    .build();
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
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        return CartMapper.toDTO(cart);
    }

    @Override
    public CartDTO updateProductInCart(ObjectId userId, ObjectId productId, int newQuantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        Product product = findProductById(productId);

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

    @Override
    public void clearCart(Cart cart) {
        log.info("Clearing cart: {}", cart.getId());
        cart.getCartItems().clear();
        cart.updateTotal();
        cartRepository.save(cart);
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
