package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.AddressDTO;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.Address;
import factory.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CheckoutMapper Tests")
class CheckoutMapperTest {

    @Nested
    @DisplayName("toDTO")
    class ToDTO {

        @Test
        @DisplayName("shouldReturnNullWhenCheckoutIsNull")
        void shouldReturnNullWhenCheckoutIsNull() {
            assertThat(CheckoutMapper.toDTO(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapFullyPopulatedCheckoutToDTO")
        void shouldMapFullyPopulatedCheckoutToDTO() {
            User user = TestDataBuilder.buildUser();
            Cart cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
            Checkout checkout = TestDataBuilder.buildPendingCheckout(user, cart);

            CheckoutDTO dto = CheckoutMapper.toDTO(checkout);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(checkout.getId().toHexString());
            assertThat(dto.getStatus()).isEqualTo("PENDING");
            assertThat(dto.getSubtotal()).isEqualTo(99.98);
            assertThat(dto.getTotalAmount()).isEqualTo(109.978);
            assertThat(dto.getUser()).isNotNull();
            assertThat(dto.getUser().getEmail()).isEqualTo("email@example.com");
        }

        @Test
        @DisplayName("shouldMapNullCartToNullCartId")
        void shouldMapNullCartToNullCartId() {
            User user = TestDataBuilder.buildUser();
            Cart cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
            Checkout checkout = TestDataBuilder.buildPendingCheckout(user, cart);
            checkout.setCart(null);

            CheckoutDTO dto = CheckoutMapper.toDTO(checkout);

            assertThat(dto.getCartId()).isNull();
        }

        @Test
        @DisplayName("shouldMapNullItemsToNullInDTO")
        void shouldMapNullItemsToNullInDTO() {
            User user = TestDataBuilder.buildUser();
            Cart cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
            Checkout checkout = TestDataBuilder.buildPendingCheckout(user, cart);
            checkout.setItems(null);

            CheckoutDTO dto = CheckoutMapper.toDTO(checkout);

            assertThat(dto.getItems()).isNull();
        }

        @Test
        @DisplayName("shouldMapAddressesToDTO")
        void shouldMapAddressesToDTO() {
            User user = TestDataBuilder.buildUser();
            Cart cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
            Checkout checkout = TestDataBuilder.buildPendingCheckout(user, cart);

            CheckoutDTO dto = CheckoutMapper.toDTO(checkout);

            assertThat(dto.getShippingAddress()).isNotNull();
            assertThat(dto.getShippingAddress().getCity()).isEqualTo("Johannesburg");
            assertThat(dto.getBillingAddress()).isNotNull();
        }

        @Test
        @DisplayName("shouldMapNullUserToNullUserDTO")
        void shouldMapNullUserToNullUserDTO() {
            User user = TestDataBuilder.buildUser();
            Cart cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
            Checkout checkout = TestDataBuilder.buildPendingCheckout(user, cart);
            checkout.setUser(null);

            CheckoutDTO dto = CheckoutMapper.toDTO(checkout);

            assertThat(dto.getUser()).isNull();
        }
    }

    @Nested
    @DisplayName("toAddress")
    class ToAddress {

        @Test
        @DisplayName("shouldReturnNullWhenAddressDTOIsNull")
        void shouldReturnNullWhenAddressDTOIsNull() {
            assertThat(CheckoutMapper.toAddress(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapAddressDTOToAddress")
        void shouldMapAddressDTOToAddress() {
            AddressDTO dto = new AddressDTO();
            dto.setStreetAddress("51 Frank Ocean Street");
            dto.setCity("Johannesburg");
            dto.setState("Gauteng");
            dto.setCountry("South Africa");
            dto.setPostalCode("2003");

            Address address = CheckoutMapper.toAddress(dto);

            assertThat(address).isNotNull();
            assertThat(address.getStreetAddress()).isEqualTo("51 Frank Ocean Street");
            assertThat(address.getCity()).isEqualTo("Johannesburg");
            assertThat(address.getPostalCode()).isEqualTo("2003");
        }
    }
}