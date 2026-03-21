package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.utils.DateUtil;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Spy
    private PaymentServiceImpl paymentService;

    private Checkout validCheckout;
    private Checkout codCheckout;

    @BeforeEach
    void setUp() {
        doNothing().when(paymentService).simulateProcessingDelay();

        Product product = Product.builder()
                .id(new ObjectId())
                .title("payment product")
                .description("Lightweight hoodie")
                .category("clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .imageUrls(List.of("", "", ""))
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();

        CartItems cartItem = CartItems.builder()
                .product(product)
                .quantity(2)
                .discount(0)
                .tax(0)
                .productPrice(99.98)
                .build();

        User user = User
                .builder()
                .id(new ObjectId())
                .name("user")
                .password("password")
                .email("email")
                .phone("0716781000")
                .build();

        Cart cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(cartItem)))
                .totalPrice(99.98)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();

        validCheckout = Checkout.builder()
                .id(new ObjectId())
                .user(user)
                .cart(cart)
                .totalAmount(109.978)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .subtotal(99.98)
                .discount(0)
                .tax(9.98)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();

        codCheckout = Checkout.builder()
                .id(new ObjectId())
                .user(user)
                .cart(cart)
                .totalAmount(109.97)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .subtotal(99.98)
                .discount(0)
                .tax(9.98)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    @Nested
    @DisplayName("processPayment - Cash on Delivery")
    class ProcessPaymentCOD {
        @Test
        @DisplayName("shouldReturnSuccessWithPendingStatusForCashOnDelivery")
        void shouldReturnSuccessWithPendingStatusForCashOnDelivery() {
            PaymentResultDTO result = paymentService.processPayment(codCheckout);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.getTransactionId()).isNotNull().isNotEmpty();
            assertThat(result.getAmountProcessed()).isEqualTo(109.978);
            assertThat(result.getPaymentMethod()).isEqualTo("CASH_ON_DELIVERY");
            assertThat(result.getMessage()).isEqualTo("Payment processed successfully");
        }

        @Test
        @DisplayName("shouldNeverFailForCashOnDelivery")
        void shouldNeverFailForCashOnDelivery() {
            for (int i = 0; i < 10; i++) {
                PaymentResultDTO result = paymentService.processPayment(codCheckout);
                assertThat(result.isSuccess())
                        .as("COD payment should always succeed (iteration %d)", i + 1)
                        .isTrue();
                assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            }
        }
    }

    @Nested
    @DisplayName("simulateProcessingDelay")
    class SimulateProcessingDelay {
        @Test
        @DisplayName("shouldNotThrowWhenDelayIsInterrupted")
        void shouldNotThrowWhenDelayIsInterrupted() {
            PaymentServiceImpl realService = new PaymentServiceImpl();

            Thread testThread = new Thread(() -> {
                org.assertj.core.api.Assertions
                        .assertThatCode(realService::simulateProcessingDelay)
                        .doesNotThrowAnyException();
            });

            testThread.start();
            testThread.interrupt();

            try {
                testThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("validatePaymentRequest")
    class ValidatePaymentRequest {
        @Test
        @DisplayName("shouldThrowPaymentExceptionWhenTotalAmountIsZero")
        void shouldThrowPaymentExceptionWhenTotalAmountIsZero() {
            validCheckout.setTotalAmount(0);

            assertThatThrownBy(() -> paymentService.validatePaymentRequest(validCheckout))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("shouldThrowPaymentExceptionWhenTotalAmountIsNegative")
        void shouldThrowPaymentExceptionWhenTotalAmountIsNegative() {

            validCheckout.setTotalAmount(-10.00);

            assertThatThrownBy(() -> paymentService.validatePaymentRequest(validCheckout))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("shouldThrowPaymentExceptionWhenPaymentMethodIsNotSelected")
        void shouldThrowPaymentExceptionWhenPaymentMethodIsNotSelected() {
            validCheckout.setPaymentMethod(PaymentMethod.NOT_SELECTED);

            assertThatThrownBy(() -> paymentService.validatePaymentRequest(validCheckout))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Payment method not selected");
        }

        @Test
        @DisplayName("shouldNotThrowWhenCheckoutIsValid")
        void shouldNotThrowWhenCheckoutIsValid() {
            org.assertj.core.api.Assertions
                    .assertThatCode(() -> paymentService.validatePaymentRequest(validCheckout))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("processPayment - Validation")
    class ProcessPaymentValidation {

        @Test
        @DisplayName("shouldThrowPaymentExceptionWhenAmountIsZeroBeforeSimulating")
        void shouldThrowPaymentExceptionWhenAmountIsZeroBeforeSimulating() {
            validCheckout.setTotalAmount(0);

            assertThatThrownBy(() -> paymentService.processPayment(validCheckout))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("shouldThrowPaymentExceptionWhenPaymentMethodNotSelectedBeforeSimulating")
        void shouldThrowPaymentExceptionWhenPaymentMethodNotSelectedBeforeSimulating() {
            validCheckout.setPaymentMethod(PaymentMethod.NOT_SELECTED);

            assertThatThrownBy(() -> paymentService.processPayment(validCheckout))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Payment method not selected");
        }
    }

    @Nested
    @DisplayName("processPayment - Credit Card")
    class ProcessPaymentCreditCard {

        @Test
        @DisplayName("shouldReturnResultWithCorrectShapeRegardlessOfSuccessOrFailure")
        void shouldReturnResultWithCorrectShapeRegardlessOfSuccessOrFailure() {
            PaymentResultDTO result = paymentService.processPayment(validCheckout);

            assertThat(result).isNotNull();
            assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
            assertThat(result.getPaymentStatus()).isIn(PaymentStatus.COMPLETED, PaymentStatus.FAILED);

            if (result.isSuccess()) {
                assertThat(result.getTransactionId()).isNotNull().isNotEmpty();
                assertThat(result.getAmountProcessed()).isEqualTo(109.978);
                assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
                assertThat(result.getFailureReason()).isNull();
            } else {
                assertThat(result.getTransactionId()).isNull();
                assertThat(result.getAmountProcessed()).isZero();
                assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
                assertThat(result.getFailureReason()).isNotNull().isNotEmpty();
            }
        }

        @RepeatedTest(20)
        @DisplayName("shouldAlwaysReturnWellFormedResultAcrossMultipleRuns")
        void shouldAlwaysReturnWellFormedResultAcrossMultipleRuns() {
            PaymentResultDTO result = paymentService.processPayment(validCheckout);

            assertThat(result).isNotNull();
            assertThat(result.getPaymentStatus()).isIn(PaymentStatus.COMPLETED, PaymentStatus.FAILED);
            assertThat(result.getMessage()).isNotNull().isNotEmpty();
            assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        }

        @Test
        @DisplayName("shouldReturnFailureReasonFromKnownListWhenPaymentFails")
        void shouldReturnFailureReasonFromKnownListWhenPaymentFails() {
            List<String> knownReasons = List.of(
                    "Insufficient funds",
                    "Card declined",
                    "Transaction timeout",
                    "Invalid card details",
                    "Payment gateway error"
            );

            boolean foundFailure = false;
            for (int i = 0; i < 50; i++) {
                PaymentResultDTO result = paymentService.processPayment(validCheckout);
                if (!result.isSuccess()) {
                    assertThat(result.getFailureReason())
                            .isIn(knownReasons);
                    assertThat(result.getMessage()).isEqualTo(result.getFailureReason());
                    foundFailure = true;
                    break;
                }
            }

            assertThat(foundFailure)
                    .as("Expected at least one payment failure in 50 attempts")
                    .isTrue();
        }
    }
}