package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.business.CheckoutValidationService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.business.PaymentService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.mapper.CheckoutMapper;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.CheckoutRepository;
import co.za.ecommerce.repository.ProductRepository;
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
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService Tests")
class CheckoutServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CheckoutRepository checkoutRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CheckoutValidationService validationService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private OrderService orderService;
    @Mock
    private CartService cartService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private ObjectId userId;
    private ObjectId checkoutId;
    private ObjectId cartId;
    private User user;
    private Product product;
    private Cart cart;
    private CartItems cartItem;
    private Checkout pendingCheckout;
    private CheckoutDTO checkoutDTO;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        checkoutId = new ObjectId();
        cartId = new ObjectId();

        user = TestDataBuilder.buildUser(userId);
        user.setId(userId);
        cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
        cart.setId(cartId);

        pendingCheckout = TestDataBuilder.buildPendingCheckout(user, cart);
        pendingCheckout.setId(checkoutId);

        checkoutDTO = CheckoutDTO.builder()
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
    }

    @Nested
    @DisplayName("CreateCheckoutFromCart")
    class CreateCheckoutFromCart {
        @Test
        @DisplayName("shouldThrowCheckoutExceptionWhenCartNotFound")
        void shouldThrowCheckoutExceptionWhenCartNotFound() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.createCheckoutFromCart(userId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("No active cart found");

            verify(checkoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowCheckoutExceptionWhenCartIsEmpty")
        void shouldThrowCheckoutExceptionWhenCartIsEmpty() {
            // Arrange
            Cart emptyCart = Cart.builder()
                    .id(cartId)
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .build();

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

            assertThatThrownBy(() -> checkoutService
                    .createCheckoutFromCart(userId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("empty cart");

            verify(checkoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldReturnExistingPendingCheckoutWhenOneAlreadyExistsForCart")
        void shouldReturnExistingPendingCheckoutWhenOneAlreadyExistsForCart() {
            // Arrange
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(checkoutRepository.findByCartId(cartId)).thenReturn(Optional.of(pendingCheckout));

            try (MockedStatic<CheckoutMapper> mapperMock = mockStatic(CheckoutMapper.class)) {
                CheckoutDTO existingDTO = CheckoutDTO.builder().status("PENDING").build();
                mapperMock.when(() -> CheckoutMapper.toDTO(pendingCheckout)).thenReturn(existingDTO);

                CheckoutDTO result = checkoutService.createCheckoutFromCart(userId);

                // Assert
                assertThat(result.getStatus()).isEqualTo("PENDING");
                verify(checkoutRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("shouldCreateNewCheckoutWithCorrectStatusAndTotalsWhenNoneExists")
        void shouldCreateNewCheckoutWithCorrectStatusAndTotalsWhenNoneExists() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(checkoutRepository.findByCartId(cartId)).thenReturn(Optional.empty());
            when(checkoutRepository.save(any(Checkout.class))).thenReturn(pendingCheckout);

            try (MockedStatic<CheckoutMapper> mapperMock = mockStatic(CheckoutMapper.class)) {
                mapperMock.when(() -> CheckoutMapper.toDTO(any(Checkout.class)))
                        .thenReturn(checkoutDTO);

                checkoutService.createCheckoutFromCart(userId);

                ArgumentCaptor<Checkout> checkoutCaptor = ArgumentCaptor.forClass(Checkout.class);
                verify(checkoutRepository).save(checkoutCaptor.capture());

                Checkout saved = checkoutCaptor.getValue();

                assertThat(saved.getStatus()).isEqualTo(CheckoutStatus.PENDING);
                assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.NOT_SELECTED);
                assertThat(saved.getCurrency()).isEqualTo("ZAR");
                assertThat(saved.getItems()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("GetCheckoutByUserId")
    class GetCheckoutByUserId {
        @Test
        @DisplayName("shouldReturnCheckoutDTOWhenCheckoutExists")
        void shouldReturnCheckoutDTOWhenCheckoutExists() {
            when(checkoutRepository.findFirstByUserId(userId)).thenReturn(Optional.of(pendingCheckout));

            try (MockedStatic<CheckoutMapper> mapperMock = mockStatic(CheckoutMapper.class)) {
                CheckoutDTO expected = CheckoutDTO.builder().status("PENDING").build();
                mapperMock.when(() -> CheckoutMapper.toDTO(pendingCheckout)).thenReturn(expected);

                CheckoutDTO result = checkoutService.getCheckoutByUserId(userId);

                assertThat(result).isNotNull();
                assertThat(result.getStatus()).isEqualTo("PENDING");
            }
        }

        @Test
        @DisplayName("shouldThrowCheckoutExceptionWhenNoCheckoutFoundForUser")
        void shouldThrowCheckoutExceptionWhenNoCheckoutFoundForUser() {
            when(checkoutRepository.findFirstByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.getCheckoutByUserId(userId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("does not have any checked out items");
        }
    }

    @Nested
    @DisplayName("GetCheckoutByCartId")
    class GetCheckoutByCartId {
    }

    @Nested
    @DisplayName("GetCheckoutsByStatus")
    class GetCheckoutsByStatus {
        @Test
        @DisplayName("shouldThrowWhenStatusIsNull")
        void shouldThrowWhenStatusIsNull() {
            assertThatThrownBy(() -> checkoutService
                    .getCheckoutsByStatus(null))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("shouldThrowWhenStatusStringIsInvalid")
        void shouldThrowWhenStatusStringIsInvalid() {
            assertThatThrownBy(() -> checkoutService
                    .getCheckoutsByStatus("INVALID_STATUS"))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Invalid checkout status");
        }

        @Test
        @DisplayName("shouldReturnCheckoutsWhenValidStatusProvided")
        void shouldReturnCheckoutsWhenValidStatusProvided() {
            when(checkoutRepository.findAllByStatus(CheckoutStatus.PENDING)).thenReturn(List.of(pendingCheckout));

            try (MockedStatic<CheckoutMapper> mapperMock = mockStatic(CheckoutMapper.class)) {
                mapperMock.when(() -> CheckoutMapper.toDTO(pendingCheckout)).thenReturn(checkoutDTO);

                List<CheckoutDTO> results = checkoutService.getCheckoutsByStatus("PENDING");

                assertThat(results).hasSize(1);
            }
        }

        @Test
        @DisplayName("shouldBeCaseInsensitiveForStatusString")
        void shouldBeCaseInsensitiveForStatusString() {
            // Arrange
            when(checkoutRepository.findAllByStatus(CheckoutStatus.PENDING)).thenReturn(List.of(pendingCheckout));

            try (MockedStatic<CheckoutMapper> mapperMock = mockStatic(CheckoutMapper.class)) {
                mapperMock.when(() -> CheckoutMapper.toDTO(any())).thenReturn(checkoutDTO);

                // Act
                List<CheckoutDTO> results = checkoutService.getCheckoutsByStatus("pending");

                assertThat(results).hasSize(1);
            }
        }
    }

    @Test
    void updateCheckout() {
    }

    @Nested
    @DisplayName("cancelCheckout")
    class CancelCheckout {
        @Test
        @DisplayName("shouldThrowWhenCheckoutNotFound")
        void shouldThrowWhenCheckoutNotFound() {
            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService
                    .cancelCheckout(checkoutId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Checkout not found");

            verify(checkoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenTryingToCancelCompletedCheckout")
        void shouldThrowWhenTryingToCancelCompletedCheckout() {
            pendingCheckout.setStatus(CheckoutStatus.COMPLETED);
            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));

            assertThatThrownBy(() -> checkoutService
                    .cancelCheckout(checkoutId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Cannot cancel checkout");

            verify(checkoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldCancelAndSaveWhenCheckoutIsPending")
        void shouldCancelAndSaveWhenCheckoutIsPending() {
            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));

            checkoutService.cancelCheckout(checkoutId);

            ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);
            verify(checkoutRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(CheckoutStatus.CANCELLED);
        }

        @Test
        @DisplayName("shouldCancelAndSaveWhenCheckoutIsFailed")
        void shouldCancelAndSaveWhenCheckoutIsFailed() {
            pendingCheckout.setStatus(CheckoutStatus.FAILED);
            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));

            checkoutService.cancelCheckout(checkoutId);

            ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);
            verify(checkoutRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(CheckoutStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("DeleteCheckoutByUserId")
    class DeleteCheckoutByUserId {
        @Test
        @DisplayName("shouldThrowWhenNoPendingCheckoutsExist")
        void shouldThrowWhenNoPendingCheckoutsExist() {
            Checkout completedCheckout = new Checkout();
            completedCheckout.setStatus(CheckoutStatus.COMPLETED);

            when(checkoutRepository.findByUserId(userId)).thenReturn(List.of(completedCheckout));

            assertThatThrownBy(() -> checkoutService
                    .deleteCheckoutByUserId(userId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("No pending checkouts found");

            verify(checkoutRepository, never()).deleteAll(any());
        }

        @Test
        @DisplayName("shouldDeleteAllPendingCheckoutsAndReturnLastOne")
        void shouldDeleteAllPendingCheckoutsAndReturnLastOne() {
            Checkout pending1 = new Checkout();
            pending1.setStatus(CheckoutStatus.PENDING);
            pending1.setUser(user);
            pending1.setCart(cart);
            pending1.setItems(new ArrayList<>());

            Checkout pending2 = new Checkout();
            pending2.setStatus(CheckoutStatus.PENDING);
            pending2.setUser(user);
            pending2.setCart(cart);
            pending2.setItems(new ArrayList<>());

            when(checkoutRepository.findByUserId(userId)).thenReturn(List.of(pending1, pending2));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Checkout.class), eq(CheckoutDTO.class))).thenReturn(checkoutDTO);

            CheckoutDTO result = checkoutService.deleteCheckoutByUserId(userId);

            verify(checkoutRepository).deleteAll(argThat(list -> ((List<?>) list).size() == 2));
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("confirmCheckout")
    class ConfirmCheckout {
        @Test
        @DisplayName("shouldThrowCheckoutExceptionWhenCheckoutNotFound")
        void shouldThrowCheckoutExceptionWhenCheckoutNotFound() {
            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService
                    .confirmCheckout(checkoutId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Checkout not found");

            verify(paymentService, never()).processPayment(any());
            verify(orderService, never()).createOrderFromCheckout(any(), any());
        }

        @Test
        @DisplayName("shouldThrowCheckoutExceptionWhenCheckoutIsNotPending")
        void shouldThrowCheckoutExceptionWhenCheckoutIsNotPending() {
            pendingCheckout.setStatus(CheckoutStatus.COMPLETED);
            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));

            assertThatThrownBy(() -> checkoutService
                    .confirmCheckout(checkoutId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Cannot confirm checkout");

            verify(paymentService, never()).processPayment(any());
        }

        @Test
        @DisplayName("shouldSetStatusToFailedAndThrowPaymentExceptionWhenPaymentFails")
        void shouldSetStatusToFailedAndThrowPaymentExceptionWhenPaymentFails() {
            // Arrange — payment returns a failure result
            PaymentResultDTO failedPayment = PaymentResultDTO.builder()
                    .success(false)
                    .failureReason("Card declined")
                    .build();

            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));
            when(checkoutRepository.save(any(Checkout.class))).thenReturn(pendingCheckout);
            doNothing().when(validationService).validateCheckout(any());
            when(paymentService.processPayment(any())).thenReturn(failedPayment);

            // Act & Assert — PaymentException should propagate up
            assertThatThrownBy(() -> checkoutService
                    .confirmCheckout(checkoutId))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Card declined");

            ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);
            verify(checkoutRepository, atLeastOnce()).save(captor.capture());

            // Find the save call that set status to FAILED
            boolean failedStatusSaved = captor.getAllValues()
                    .stream()
                    .anyMatch(c -> CheckoutStatus.FAILED.equals(c.getStatus()));
            assertThat(failedStatusSaved).isTrue();

            // Order must NEVER be created when payment fails
            verify(orderService, never()).createOrderFromCheckout(any(), any());
            // Cart must NEVER be cleared when payment fails
            verify(cartService, never()).clearCart(any());
        }

        @Test
        @DisplayName("shouldCreateOrderSetCompletedAndClearCartWhenPaymentSucceeds")
        void shouldCreateOrderSetCompletedAndClearCartWhenPaymentSucceeds() {
            PaymentResultDTO successPayment = PaymentResultDTO.builder()
                    .success(true)
                    .transactionId("TXN-123456")
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .amountProcessed(109.978)
                    .build();

            OrderDTO orderDTO = OrderDTO.builder()
                    .transactionId("TXN-123456")
                    .build();

            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));
            when(checkoutRepository.save(any(Checkout.class))).thenReturn(pendingCheckout);
            doNothing().when(validationService).validateCheckout(any());
            when(paymentService.processPayment(any())).thenReturn(successPayment);
            when(orderService.createOrderFromCheckout(any(), any())).thenReturn(orderDTO);

            // Act
            OrderDTO result = checkoutService.confirmCheckout(checkoutId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTransactionId()).isEqualTo("TXN-123456");

            // Checkout status must be set to COMPLETED
            ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);
            verify(checkoutRepository, atLeastOnce()).save(captor.capture());

            boolean completedStatusSaved = captor.getAllValues().stream().anyMatch(c -> CheckoutStatus.COMPLETED.equals(c.getStatus()));
            assertThat(completedStatusSaved).isTrue();

            // Order must be created exactly once
            verify(orderService, times(1)).createOrderFromCheckout(any(), any());

            // Cart must be cleared after successful order
            verify(cartService, times(1)).clearCart(cart);
        }

        @Test
        @DisplayName("shouldSetStatusToFailedAndThrowCheckoutExceptionOnUnexpectedError")
        void shouldSetStatusToFailedAndThrowCheckoutExceptionOnUnexpectedError() {
            PaymentResultDTO successPayment = PaymentResultDTO.builder()
                    .success(true)
                    .transactionId("TXN-999")
                    .build();

            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));
            when(checkoutRepository.save(any(Checkout.class))).thenReturn(pendingCheckout);
            doNothing().when(validationService).validateCheckout(any());
            when(paymentService.processPayment(any())).thenReturn(successPayment);
            when(orderService.createOrderFromCheckout(any(), any())).thenThrow(new RuntimeException("Database connection lost"));

            // Act & Assert
            assertThatThrownBy(() -> checkoutService
                    .confirmCheckout(checkoutId))
                    .isInstanceOf(CheckoutException.class)
                    .hasMessageContaining("Error during checkout confirmation");

            // Status must be FAILED to prevent the user from being stuck
            ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);
            verify(checkoutRepository, atLeastOnce()).save(captor.capture());

            boolean failedStatusSaved = captor.getAllValues().stream().anyMatch(c -> CheckoutStatus.FAILED.equals(c.getStatus()));
            assertThat(failedStatusSaved).isTrue();
        }

        @Test
        @DisplayName("shouldRunValidationBeforeProcessingPayment")
        void shouldRunValidationBeforeProcessingPayment() {
            PaymentResultDTO successPayment = PaymentResultDTO.builder()
                    .success(true)
                    .transactionId("TXN-ORDER")
                    .build();

            when(checkoutRepository.findById(checkoutId)).thenReturn(Optional.of(pendingCheckout));
            when(checkoutRepository.save(any(Checkout.class))).thenReturn(pendingCheckout);
            doNothing().when(validationService).validateCheckout(any());
            when(paymentService.processPayment(any())).thenReturn(successPayment);
            when(orderService.createOrderFromCheckout(any(), any())).thenReturn(OrderDTO.builder().transactionId("TXN-ORDER").build());

            checkoutService.confirmCheckout(checkoutId);

            // Verify call order: validation → payment → order creation
            var inOrder = inOrder(validationService, paymentService, orderService);
            inOrder.verify(validationService).validateCheckout(any());
            inOrder.verify(paymentService).processPayment(any());
            inOrder.verify(orderService).createOrderFromCheckout(any(), any());
        }
    }
}