package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.UserNotFoundException;
import co.za.ecommerce.mapper.CartMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.utils.DateUtil;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private ObjectId userId;
    private ObjectId productId;
    private User user;
    private Product product;
    private Cart cart;
    private CartItems cartItem;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        productId = new ObjectId();

        user = new User();
        user.setId(userId);

        product = Product.builder()
                .id(productId)
                .title("Organic Fleece Hoodie")
                .price(49.99)
                .quantity(25)
                .imageUrls(List.of("https://s3.amazonaws.com/hoodie.jpg"))
                .build();

        // Build a cart item that already references our product
        cartItem = CartItems.builder()
                .product(product)
                .quantity(2)
                .discount(0)
                .tax(0)
                .productPrice(99.98)
                .build();

        // Build a cart with one existing item already in it.
        // We use a real ArrayList so tests can mutate it (add/remove).
        // Using List.of() would throw UnsupportedOperationException on mutation.
        cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(cartItem)))
                .totalPrice(99.98)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    // addProductToCart
    //
    // This is the most complex method in the service with several branches:
    // 1. Cart exists vs cart doesn't exist (auto-create)
    // 2. Product exists vs doesn't exist
    // 3. Product has stock vs out of stock
    // 4. Item already in cart (accumulate) vs new item (add)
    // 5. Requested quantity exceeds available stock
    //
    // For each branch we ask: what should happen, and what should NOT happen?
    // The "should NOT happen" assertions (verify never) are just as important
    // as the positive ones — they confirm we fail fast and don't do extra work.
    @Nested
    @DisplayName("AddProductToCart")
    class AddProductToCart {
        @Test
        @DisplayName("shouldCreateNewCartAndAddProductWhenUserHasNoCart")
        void shouldCreateNewCartAndAddProductWhenUserHasNoCart() {
            // Arrange
            // Simulate: user has no existing cart → service should create one.
            // The first findByUserId returns empty, then save() returns a fresh cart.
            Cart emptyCart = Cart.builder()
                    .id(new ObjectId())
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .totalPrice(0.0)
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            // First save() creates the empty cart, second save() persists it with the item
            when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            // Act — CartMapper.toDTO is a static method so we use MockedStatic
            // to intercept it without needing a real Cart → DTO mapping
            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                CartDTO expectedDTO = new CartDTO();
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(expectedDTO);

                CartDTO result = cartService.addProductToCart(userId, productId, 1);

                // Assert
                assertThat(result).isNotNull();
                // userRepository must be called to look up the user for the new cart
                verify(userRepository).findById(userId);
                // productRepository must be called to reduce stock
                verify(productRepository).save(any(Product.class));
            }
        }

        @Test
        @DisplayName("shouldAddNewItemToCartWhenProductNotAlreadyInCart")
        void shouldAddNewItemToCartWhenProductNotAlreadyInCart() {
            // Arrange
            // Existing cart has the hoodie (cartItem). We're adding a different product.
            // The service should add a NEW CartItems entry to the list.
            ObjectId newProductId = new ObjectId();
            Product newProduct = Product.builder()
                    .id(newProductId)
                    .title("Stretch Slim-Fit Jeans")
                    .price(59.99)
                    .quantity(30)
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(newProductId)).thenReturn(Optional.of(newProduct));
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.addProductToCart(userId, newProductId, 2);

                // Assert — the cart should now have 2 items (original hoodie + new jeans)
                ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
                verify(cartRepository).save(cartCaptor.capture());
                assertThat(cartCaptor.getValue().getCartItems()).hasSize(2);
            }
        }

        @Test
        @DisplayName("shouldAccumulateQuantityWhenProductAlreadyExistsInCart")
        void shouldAccumulateQuantityWhenProductAlreadyExistsInCart() {
            // Arrange
            // The cart already has 2 hoodies (cartItem). Adding 3 more should give 5.
            // The service must NOT add a second CartItems entry — it should update the existing one.
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.addProductToCart(userId, productId, 3);

                // Assert — still only 1 item in cart (quantity accumulated, not duplicated)
                ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
                verify(cartRepository).save(cartCaptor.capture());
                List<CartItems> items = cartCaptor.getValue().getCartItems();
                assertThat(items).hasSize(1);
                assertThat(items.get(0).getQuantity()).isEqualTo(5); // 2 existing + 3 new
            }
        }

        @Test
        @DisplayName("shouldReduceProductStockByRequestedQuantityAfterAdding")
        void shouldReduceProductStockByRequestedQuantityAfterAdding() {
            // Arrange
            // Product starts with 25 units. Adding 3 should reduce it to 22.
            // This verifies the inventory management logic — if we forget to reduce
            // stock, users could order more than available.
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.addProductToCart(userId, productId, 3);

                // Capture the product saved to the repository and check its quantity
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(22); // 25 - 3
            }
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenProductIsOutOfStock")
        void shouldThrowProductExceptionWhenProductIsOutOfStock() {
            // Arrange — product has 0 stock
            Product outOfStockProduct = Product.builder()
                    .id(productId)
                    .title("Sold Out Hoodie")
                    .price(49.99)
                    .quantity(0)
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(outOfStockProduct));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addProductToCart(userId, productId, 1))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("not available");

            // Nothing should be saved when stock validation fails
            verify(cartRepository, never()).save(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenRequestedQuantityExceedsStock")
        void shouldThrowProductExceptionWhenRequestedQuantityExceedsStock() {
            // Arrange — product has 5 units, requesting 10
            Product lowStockProduct = Product.builder()
                    .id(productId)
                    .title("Low Stock Hoodie")
                    .price(49.99)
                    .quantity(5)
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(lowStockProduct));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addProductToCart(userId, productId, 10))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("less than or equal to");

            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenProductNotFound")
        void shouldThrowProductExceptionWhenProductNotFound() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addProductToCart(userId, productId, 1))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("Product not found");

            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowUserNotFoundExceptionWhenCreatingCartForNonExistentUser")
        void shouldThrowUserNotFoundExceptionWhenCreatingCartForNonExistentUser() {
            // Arrange — no cart exists and user doesn't exist either
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addProductToCart(userId, productId, 1))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(cartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("GetUserCartWithItems")
    class GetUserCartWithItems {
        @Test
        @DisplayName("shouldReturnExistingCartWhenCartFound")
        void shouldReturnExistingCartWhenCartFound() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                CartDTO expectedDTO = CartDTO.builder()
                        .totalPrice(99.98)
                        .build();
                cartMapperMock.when(() -> CartMapper.toDTO(cart)).thenReturn(expectedDTO);

                CartDTO result = cartService.getUserCartWithItems(userId);

                assertThat(result).isNotNull();
                assertThat(result.getTotalPrice()).isEqualTo(99.98);
                // No new cart should be created when one already exists
                verify(userRepository, never()).findById(any());
            }
        }

        @Test
        @DisplayName("shouldCreateAndReturnNewCartWhenNoneExists")
        void shouldCreateAndReturnNewCartWhenNoneExists() {
            Cart newCart = Cart.builder()
                    .id(new ObjectId())
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .totalPrice(0.0)
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.getUserCartWithItems(userId);

                // A new cart should have been created and saved
                verify(cartRepository).save(any(Cart.class));
                verify(userRepository).findById(userId);
            }
        }
    }

    @Nested
    @DisplayName("updateProductInCart")
    class UpdateProductInCart {
        @Test
        @DisplayName("shouldThrowCartExceptionWhenCartNotFound")
        void shouldThrowCartExceptionWhenCartNotFound() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateProductInCart(userId, productId, 3))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("Cart not found");

            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowCartExceptionWhenProductNotFoundInCart")
        void shouldThrowCartExceptionWhenProductNotFoundInCart() {
            // Arrange — cart exists but contains a different product
            ObjectId differentProductId = new ObjectId();
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(differentProductId)).thenReturn(Optional.of(product));

            assertThatThrownBy(() ->
                    cartService.updateProductInCart(userId, differentProductId, 3))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("Product not found in cart");
        }

        @Test
        @DisplayName("shouldRemoveItemAndRestoreStockWhenNewQuantityIsZero")
        void shouldRemoveItemAndRestoreStockWhenNewQuantityIsZero() {
            // Arrange
            // Cart has 2 hoodies. Setting quantity to 0 should:
            // 1. Remove the item from the cart entirely
            // 2. Return the 2 units back to the product stock
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, 0);

                // Cart should now be empty
                assertThat(cart.getCartItems()).isEmpty();

                // Stock should be restored: 25 original + 2 from cart = 27
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(27);
            }
        }

        @Test
        @DisplayName("shouldRemoveItemAndRestoreStockWhenNewQuantityIsNegative")
        void shouldRemoveItemAndRestoreStockWhenNewQuantityIsNegative() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, -1);

                assertThat(cart.getCartItems()).isEmpty();
                verify(productRepository).save(any(Product.class));
            }
        }

        @Test
        @DisplayName("shouldUpdateQuantityAndAdjustStockByDeltaWhenIncreasing")
        void shouldUpdateQuantityAndAdjustStockByDeltaWhenIncreasing() {
            // Arrange
            // Cart has 2 hoodies. Updating to 5 means requesting 3 more.
            // Delta = 5 - 2 = +3 → stock reduces by 3 (25 → 22)
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, 5);

                // Cart item quantity should be 5
                assertThat(cart.getCartItems().get(0).getQuantity()).isEqualTo(5);

                // Stock: 25 - (5 - 2) = 22
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(22);
            }
        }

        @Test
        @DisplayName("shouldUpdateQuantityAndRestoreStockByDeltaWhenDecreasing")
        void shouldUpdateQuantityAndRestoreStockByDeltaWhenDecreasing() {
            // Arrange
            // Cart has 2 hoodies. Updating to 1 means returning 1.
            // Delta = 1 - 2 = -1 → stock increases by 1 (25 → 26)
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, 1);

                assertThat(cart.getCartItems().get(0).getQuantity()).isEqualTo(1);

                // Stock: 25 - (1 - 2) = 26
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(26);
            }
        }
    }

    // deleteProductFromCart
    //
    // Logic being tested:
    // 1. Cart not found → throw CartException
    // 2. Product not found in cart → throw CartException
    // 3. Product found → remove from cart, restore stock, save
    //
    // Removing from cart is permanent — the item is gone and stock is returned.
    // This differs from updateProductInCart(qty=0) in that it's intentional removal.
    @Nested
    @DisplayName("deleteProductFromCart")
    class DeleteProductFromCart {
        @Test
        @DisplayName("shouldRemoveItemAndRestoreStockWhenProductExistsInCart")
        void shouldRemoveItemAndRestoreStockWhenProductExistsInCart() {
            // Arrange
            // Cart has 2 hoodies. Deleting restores 2 units to the product.
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.deleteProductFromCart(userId, productId);

                // Cart should be empty after deletion
                assertThat(cart.getCartItems()).isEmpty();

                // Stock restored: 25 + 2 = 27
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(27);
            }
        }

        @Test
        @DisplayName("shouldThrowCartExceptionWhenCartNotFound")
        void shouldThrowCartExceptionWhenCartNotFound() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.deleteProductFromCart(userId, productId))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("Cart not found");

            verify(productRepository, never()).save(any());
            verify(cartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("clearCart")
    class ClearCart {
        @Test
        @DisplayName("shouldClearItemsUpdateTotalAndSaveCart")
        void shouldClearItemsUpdateTotalAndSaveCart() {
            // Arrange — cart has items before clearing
            assertThat(cart.getCartItems()).isNotEmpty();

            // Act
            cartService.clearCart(cart);

            // Assert
            assertThat(cart.getCartItems()).isEmpty();
            assertThat(cart.getTotalPrice()).isZero();
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("shouldSaveCartEvenWhenCartIsAlreadyEmpty")
        void shouldSaveCartEvenWhenCartIsAlreadyEmpty() {
            // Arrange — cart is already empty
            Cart emptyCart = Cart.builder()
                    .id(new ObjectId())
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .totalPrice(0.0)
                    .build();

            // Act
            cartService.clearCart(emptyCart);

            // Assert — save still called even on already-empty cart
            assertThat(emptyCart.getCartItems()).isEmpty();
            verify(cartRepository).save(emptyCart);
        }
    }
}