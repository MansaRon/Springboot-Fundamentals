package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import factory.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartMapper Tests")
class CartMapperTest {

    @Nested
    @DisplayName("toDTO")
    class ToDTO {

        @Test
        @DisplayName("shouldReturnNullWhenCartIsNull")
        void shouldReturnNullWhenCartIsNull() {
            assertThat(CartMapper.toDTO(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapCartWithItemsToDTO")
        void shouldMapCartWithItemsToDTO() {
            User user = TestDataBuilder.buildUser();
            Product product = TestDataBuilder.buildProduct();
            Cart cart = TestDataBuilder.buildCart(user, product);

            CartDTO dto = CartMapper.toDTO(cart);

            assertThat(dto).isNotNull();
            assertThat(dto.getCartItems()).hasSize(1);
            CartItemsDTO itemDTO = dto.getCartItems().get(0);
            assertThat(itemDTO.getQuantity()).isEqualTo(2);
            assertThat(itemDTO.getProductDTO()).isNotNull();
            assertThat(itemDTO.getProductDTO().getTitle()).isEqualTo("Organic Fleece Hoodie");
        }

        @Test
        @DisplayName("shouldMapCartWithNullItemsToEmptyDTO")
        void shouldMapCartWithNullItemsToEmptyDTO() {
            User user = TestDataBuilder.buildUser();
            Cart cart = TestDataBuilder.buildEmptyCart(user);
            cart.setCartItems(null);

            CartDTO dto = CartMapper.toDTO(cart);

            assertThat(dto).isNotNull();
            assertThat(dto.getCartItems()).isNull();
        }

        @Test
        @DisplayName("shouldMapProductFieldsCorrectlyInCartItemsDTO")
        void shouldMapProductFieldsCorrectlyInCartItemsDTO() {
            Product product = TestDataBuilder.buildProduct();
            CartItems item = TestDataBuilder.buildCartItem(product, 3);
            User user = TestDataBuilder.buildUser();
            Cart cart = Cart.builder()
                    .user(user)
                    .cartItems(List.of(item))
                    .build();

            CartDTO dto = CartMapper.toDTO(cart);
            ProductDTO productDTO = dto.getCartItems().get(0).getProductDTO();

            assertThat(productDTO.getPrice()).isEqualTo(49.99);
            assertThat(productDTO.getCategory()).isEqualTo("Clothing");
            assertThat(productDTO.getRate()).isEqualTo("4.5");
            assertThat(productDTO.getQuantity()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("fromDTO")
    class FromDTO {

        @Test
        @DisplayName("shouldReturnNullWhenCartDTOIsNull")
        void shouldReturnNullWhenCartDTOIsNull() {
            assertThat(CartMapper.fromDTO(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapCartDTOToCart")
        void shouldMapCartDTOToCart() {
            Product product = TestDataBuilder.buildProduct();
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId().toHexString());
            productDTO.setTitle(product.getTitle());
            productDTO.setPrice(product.getPrice());
            productDTO.setCategory(product.getCategory());
            productDTO.setRate(product.getRate());
            productDTO.setQuantity(product.getQuantity());

            CartItemsDTO itemDTO = new CartItemsDTO();
            itemDTO.setQuantity(2);
            itemDTO.setDiscount(0);
            itemDTO.setTax(0);
            itemDTO.setProductPrice(99.98);
            itemDTO.setProductDTO(productDTO);

            CartDTO cartDTO = new CartDTO();
            cartDTO.setCartItems(List.of(itemDTO));

            Cart cart = CartMapper.fromDTO(cartDTO);

            assertThat(cart).isNotNull();
            assertThat(cart.getCartItems()).hasSize(1);
            assertThat(cart.getCartItems().get(0).getQuantity()).isEqualTo(2);
            assertThat(cart.getCartItems().get(0).getProduct().getTitle()).isEqualTo("Organic Fleece Hoodie");
        }

        @Test
        @DisplayName("shouldMapCartDTOWithNullItemsToCartWithNullItems")
        void shouldMapCartDTOWithNullItemsToCartWithNullItems() {
            CartDTO cartDTO = new CartDTO();
            cartDTO.setCartItems(null);

            Cart cart = CartMapper.fromDTO(cartDTO);

            assertThat(cart).isNotNull();
            assertThat(cart.getCartItems()).isNull();
        }
    }
}