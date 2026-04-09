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
        Cart cart = cartRepository
                .findByUserId(userId).orElseGet(() -> createNewCartForUser(userId));

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

        CartItems cartItem = findCartItem(cart, productId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Product not found in cart.",
                        HttpStatus.NOT_FOUND.value()));

            if (newQuantity <= 0) {
                cart.getCartItems().remove(cartItem);
                product.setQuantity(product.getQuantity() + cartItem.getQuantity());
            } else {
                int previousQuantity = cartItem.getQuantity();
                int stockDelta = newQuantity - previousQuantity;
                int updatedStock = product.getQuantity() - stockDelta;

                cartItem.setQuantity(newQuantity);
                cartItem.setProductPrice(product.getPrice() * newQuantity);
                product.setQuantity(updatedStock);
            }

            productRepository.save(product);
            cart.updateTotal();
            cartRepository.save(cart);

            return CartMapper.toDTO(cart);
    }

    @Override
    public CartDTO deleteProductFromCart(ObjectId userId, ObjectId productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        CartItems cartItems = findCartItem(cart, productId).orElseThrow(() -> new CartException(
                HttpStatus.NOT_FOUND.toString(),
                "Product not found in cart.",
                HttpStatus.NOT_FOUND.value()));

        Product product = findProductById(productId);

        product.setQuantity(product.getQuantity() + cartItems.getQuantity());
        productRepository.save(product);

        cart.getCartItems().remove(cartItems);
        cart.updateTotal();

        Cart updatedCart = cartRepository.save(cart);

        return CartMapper.toDTO(updatedCart);
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

    private Product findProductById(ObjectId productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Product not found.",
                        HttpStatus.NOT_FOUND.value()));
    }

    private Optional<CartItems> findCartItem(Cart cart, ObjectId productId) {
        return cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    private void validateProductStock(Product product, int requestedQuantity) {
        if (product.getQuantity() == 0) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    product.getTitle() + " is not available.",
                    HttpStatus.BAD_REQUEST.value());
        }
        if (product.getQuantity() < requestedQuantity) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Please order " + product.getTitle() + " in a quantity less than or equal to " + product.getQuantity() + ".",
                    HttpStatus.BAD_REQUEST.value());
        }
    }
}
