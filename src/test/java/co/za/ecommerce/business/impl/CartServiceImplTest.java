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
import factory.TestDataBuilder;
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

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CartServiceImpl cartService;

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

        user = TestDataBuilder.buildUser(userId);
        user.setId(userId);

        product = TestDataBuilder.buildProduct(productId);

        cartItem = TestDataBuilder.buildCartItem(product, 2);

        cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(cartItem)))
                .totalPrice(99.98)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    @Nested
    @DisplayName("AddProductToCart")
    class AddProductToCart {
        @Test
        @DisplayName("shouldCreateNewCartAndAddProductWhenUserHasNoCart")
        void shouldCreateNewCartAndAddProductWhenUserHasNoCart() {
            Cart emptyCart = Cart.builder()
                    .id(new ObjectId())
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .totalPrice(0.0)
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                CartDTO expectedDTO = new CartDTO();
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(expectedDTO);

                CartDTO result = cartService.addProductToCart(userId, productId, 1);

                // Assert
                assertThat(result).isNotNull();

                verify(userRepository).findById(userId);
                verify(productRepository).save(any(Product.class));
            }
        }

        @Test
        @DisplayName("shouldAddNewItemToCartWhenProductNotAlreadyInCart")
        void shouldAddNewItemToCartWhenProductNotAlreadyInCart() {
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

                ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
                verify(cartRepository).save(cartCaptor.capture());
                assertThat(cartCaptor.getValue().getCartItems()).hasSize(2);
            }
        }

        @Test
        @DisplayName("shouldAccumulateQuantityWhenProductAlreadyExistsInCart")
        void shouldAccumulateQuantityWhenProductAlreadyExistsInCart() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.addProductToCart(userId, productId, 3);

                ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
                verify(cartRepository).save(cartCaptor.capture());
                List<CartItems> items = cartCaptor.getValue().getCartItems();
                assertThat(items).hasSize(1);
                assertThat(items.get(0).getQuantity()).isEqualTo(5);
            }
        }

        @Test
        @DisplayName("shouldReduceProductStockByRequestedQuantityAfterAdding")
        void shouldReduceProductStockByRequestedQuantityAfterAdding() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.addProductToCart(userId, productId, 3);

                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(22);
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
                        .build();
                cartMapperMock.when(() -> CartMapper.toDTO(cart)).thenReturn(expectedDTO);

                CartDTO result = cartService.getUserCartWithItems(userId);

                assertThat(result).isNotNull();

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
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, 0);

                assertThat(cart.getCartItems()).isEmpty();

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
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, 5);

                assertThat(cart.getCartItems().get(0).getQuantity()).isEqualTo(5);

                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(22);
            }
        }

        @Test
        @DisplayName("shouldUpdateQuantityAndRestoreStockByDeltaWhenDecreasing")
        void shouldUpdateQuantityAndRestoreStockByDeltaWhenDecreasing() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.updateProductInCart(userId, productId, 1);

                assertThat(cart.getCartItems().get(0).getQuantity()).isEqualTo(1);

                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());
                assertThat(productCaptor.getValue().getQuantity()).isEqualTo(26);
            }
        }
    }

    @Nested
    @DisplayName("deleteProductFromCart")
    class DeleteProductFromCart {
        @Test
        @DisplayName("shouldRemoveItemAndRestoreStockWhenProductExistsInCart")
        void shouldRemoveItemAndRestoreStockWhenProductExistsInCart() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            try (MockedStatic<CartMapper> cartMapperMock = mockStatic(CartMapper.class)) {
                cartMapperMock.when(() -> CartMapper.toDTO(any(Cart.class))).thenReturn(new CartDTO());

                cartService.deleteProductFromCart(userId, productId);

                assertThat(cart.getCartItems()).isEmpty();

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
            Cart emptyCart = Cart.builder()
                    .id(new ObjectId())
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .totalPrice(0.0)
                    .build();

            cartService.clearCart(emptyCart);

            assertThat(emptyCart.getCartItems()).isEmpty();
            verify(cartRepository).save(emptyCart);
        }
    }
}