package co.za.ecommerce.business.impl;

import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.repository.ProductRepository;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutValidationService Tests")
class CheckoutValidationServiceImplTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private CheckoutValidationServiceImpl validationService;

    private Product product;
    private CartItems cartItem;
    private Checkout validCheckout;

    @BeforeEach
    void setUp() {
        User user = TestDataBuilder.buildUser();
        product = TestDataBuilder.buildProduct();
        cartItem = TestDataBuilder.buildCartItem(product, 2);

        Cart cart = TestDataBuilder.buildCart(user, product);

        validCheckout = TestDataBuilder.buildPendingCheckout(user, cart);
        validCheckout.setStatus(CheckoutStatus.PENDING);
        validCheckout.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        validCheckout.setShippingMethod(DeliverMethod.DHL);
        validCheckout.setShippingAddress(TestDataBuilder.buildAddress());
        validCheckout.setBillingAddress(TestDataBuilder.buildAddress());
        validCheckout.setSubtotal(99.98);
        validCheckout.setDiscount(0);
        validCheckout.setTax(9.998);
        validCheckout.setTotalAmount(109.978);
        validCheckout.setItems(List.of(cartItem));
    }

    @Nested
    @DisplayName("ValidateCheckout")
    class ValidateCheckout {

        @Test
        @DisplayName("shouldPassValidationForFullyConfiguredCheckout")
        void shouldPassValidationForFullyConfiguredCheckout() {
            when(productRepository.findById(any())).thenReturn(Optional.of(product));

            assertThatCode(() -> validationService.validateCheckout(validCheckout))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shouldThrowWhenCheckoutStatusIsCompleted")
        void shouldThrowWhenCheckoutStatusIsCompleted() {
            validCheckout.setStatus(CheckoutStatus.COMPLETED);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Cannot process checkout");
        }

        @Test
        @DisplayName("shouldThrowWhenCheckoutStatusIsCancelled")
        void shouldThrowWhenCheckoutStatusIsCancelled() {
            validCheckout.setStatus(CheckoutStatus.CANCELLED);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Cannot process checkout");
        }

        @Test
        @DisplayName("shouldPassValidationForFailedCheckout")
        void shouldPassValidationForFailedCheckout() {
            validCheckout.setStatus(CheckoutStatus.FAILED);
            when(productRepository.findById(any())).thenReturn(Optional.of(product));

            assertThatCode(() -> validationService.validateCheckout(validCheckout))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shouldThrowWhenPaymentMethodIsNotSelected")
        void shouldThrowWhenPaymentMethodIsNotSelected() {
            validCheckout.setPaymentMethod(PaymentMethod.NOT_SELECTED);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Payment method is required");
        }

        @Test
        @DisplayName("shouldPassForCashOnDeliveryPaymentMethod")
        void shouldPassForCashOnDeliveryPaymentMethod() {
            validCheckout.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            when(productRepository.findById(any())).thenReturn(Optional.of(product));

            assertThatCode(() -> validationService.validateCheckout(validCheckout))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shouldThrowWhenShippingAddressIsNull")
        void shouldThrowWhenShippingAddressIsNull() {
            validCheckout.setShippingAddress(null);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Shipping address is required");
        }

        @Test
        @DisplayName("shouldThrowWhenBillingAddressIsNull")
        void shouldThrowWhenBillingAddressIsNull() {
            validCheckout.setBillingAddress(null);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Billing address is required");
        }

        @Test
        @DisplayName("shouldThrowWhenShippingStreetAddressIsMissing")
        void shouldThrowWhenShippingStreetAddressIsMissing() {
            Address badAddress = Address.builder()
                    .city("Johannesburg")
                    .postalCode("2003")
                    .build();
            validCheckout.setShippingAddress(badAddress);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Shipping street address is required");
        }

        @Test
        @DisplayName("shouldThrowWhenShippingCityIsMissing")
        void shouldThrowWhenShippingCityIsMissing() {
            Address badAddress = Address.builder()
                    .streetAddress("51 Frank Ocean Street")
                    .postalCode("2003")
                    .build();
            validCheckout.setShippingAddress(badAddress);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Shipping city is required");
        }

        @Test
        @DisplayName("shouldThrowWhenShippingPostalCodeIsMissing")
        void shouldThrowWhenShippingPostalCodeIsMissing() {
            Address badAddress = Address.builder()
                    .streetAddress("51 Frank Ocean Street")
                    .city("Johannesburg")
                    .build();
            validCheckout.setShippingAddress(badAddress);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Shipping postal code is required");
        }

        @Test
        @DisplayName("shouldThrowWhenShippingMethodIsNull")
        void shouldThrowWhenShippingMethodIsNull() {
            validCheckout.setShippingMethod(null);

            assertThatThrownBy(() -> validationService.validateCheckout(validCheckout))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Shipping method is required");
        }
    }

    @Nested
    @DisplayName("ValidateInventory")
    class ValidateInventory {

        @Test
        @DisplayName("shouldPassWhenInventoryIsSufficient")
        void shouldPassWhenInventoryIsSufficient() {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            assertThatCode(() -> validationService.validateInventory(List.of(cartItem)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shouldThrowWhenProductNotFound")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> validationService.validateInventory(List.of(cartItem)))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("shouldThrowWhenQuantityIsInsufficient")
        void shouldThrowWhenQuantityIsInsufficient() {
            product.setQuantity(1);
            CartItems highQtyItem = TestDataBuilder.buildCartItem(product, 10);
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> validationService.validateInventory(List.of(highQtyItem)))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("shouldThrowWhenPriceHasChanged")
        void shouldThrowWhenPriceHasChanged() {
            Product dbProduct = TestDataBuilder.buildProduct(product.getId());
            dbProduct.setPrice(99.99);

            CartItems itemWithStalePrice = TestDataBuilder.buildCartItem(product, 1);
            itemWithStalePrice.getProduct().setPrice(10.00);

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(dbProduct));

            assertThatThrownBy(() -> validationService.validateInventory(List.of(itemWithStalePrice)))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Price has changed");
        }
    }
}