package co.za.ecommerce.model;

import factory.TestDataBuilder;
import org.assertj.core.data.Offset;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cart Test")
class CartTest {

    @Test
    @DisplayName("shouldCalculateTotalCorrectlyForSingleItem")
    void shouldCalculateTotalCorrectlyForSingleItem() {
        User user = TestDataBuilder.buildUser();
        Product product = TestDataBuilder.buildProduct();
        CartItems item = TestDataBuilder.buildCartItem(product, 2);

        Cart cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(item)))
                .totalPrice(0.0)
                .build();

        cart.updateTotal();

        assertThat(cart.getTotalPrice()).isEqualTo(99.98, Offset.offset(0.001));
    }

    @Test
    @DisplayName("shouldSetTotalToZeroWhenCartIsEmpty")
    void shouldSetTotalToZeroWhenCartIsEmpty() {
        User user = TestDataBuilder.buildUser();
        Cart cart = TestDataBuilder.buildEmptyCart(user);

        cart.updateTotal();

        assertThat(cart.getTotalPrice()).isZero();
    }

    @Test
    @DisplayName("shouldReflectNewTotalAfterAddingItem")
    void shouldReflectNewTotalAfterAddingItem() {
        User user = TestDataBuilder.buildUser();
        Product hoodie = TestDataBuilder.buildProduct();
        CartItems hoodieItem = TestDataBuilder.buildCartItem(hoodie, 2);

        Cart cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(hoodieItem)))
                .totalPrice(0.0)
                .build();

        cart.updateTotal();
        assertThat(cart.getTotalPrice()).isEqualTo(99.98, Offset.offset(0.001));

        Product jeans = TestDataBuilder.buildProduct();
        jeans.setPrice(59.99);
        CartItems jeansItem = TestDataBuilder.buildCartItem(jeans, 1);
        cart.getCartItems().add(jeansItem);
        cart.updateTotal();

        assertThat(cart.getTotalPrice()).isEqualTo(159.97, Offset.offset(0.001));
    }

    @Test
    @DisplayName("shouldReflectNewTotalAfterRemovingItem")
    void shouldReflectNewTotalAfterRemovingItem() {
        User user = TestDataBuilder.buildUser();

        Product hoodie = TestDataBuilder.buildProduct();
        hoodie.setPrice(49.99);
        CartItems hoodieItem = TestDataBuilder.buildCartItem(hoodie, 2);

        Product jeans = TestDataBuilder.buildProduct();
        jeans.setPrice(59.99);
        CartItems jeansItem = TestDataBuilder.buildCartItem(jeans, 1);

        Cart cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(hoodieItem, jeansItem)))
                .totalPrice(0.0)
                .build();

        cart.updateTotal();
        assertThat(cart.getTotalPrice()).isEqualTo(159.97, Offset.offset(0.001));

        cart.getCartItems().remove(jeansItem);
        cart.updateTotal();

        assertThat(cart.getTotalPrice()).isEqualTo(99.98, Offset.offset(0.001));
    }
}