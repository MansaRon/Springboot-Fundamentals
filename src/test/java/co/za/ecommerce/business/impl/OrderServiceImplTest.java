package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderStatisticsDTO;
import co.za.ecommerce.dto.order.OrderTrackingDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.InventoryException;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.mapper.OrderMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.model.order.OrderStatus;
import co.za.ecommerce.model.order.OrderStatusHistory;
import co.za.ecommerce.repository.OrderRepository;
import co.za.ecommerce.utils.DateUtil;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryService inventoryService;
    @Mock private co.za.ecommerce.business.EmailService emailService;
    @InjectMocks private OrderServiceImpl orderService;

    private ObjectId orderId;
    private ObjectId userId;
    private User user;
    private Cart cart;
    private Checkout checkout;
    private PaymentResultDTO successfulPayment;
    private Order savedOrder;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderId = new ObjectId();
        userId = new ObjectId();

        user = TestDataBuilder.buildUser(userId);
        cart = TestDataBuilder.buildCart(user, TestDataBuilder.buildProduct());
        checkout = TestDataBuilder.buildPendingCheckout(user, cart);

        successfulPayment = PaymentResultDTO.builder()
                .success(true)
                .transactionId("TXN-20260314-483920")
                .paymentStatus(PaymentStatus.COMPLETED)
                .amountProcessed(109.978)
                .paymentMethod("CREDIT_CARD")
                .build();

        savedOrder = TestDataBuilder.buildOrder(user);
        savedOrder.setId(orderId);

        orderDTO = OrderDTO.builder()
                .orderNumber("ORD-20260314-483920")
                .orderStatus(OrderStatus.CONFIRMED.name())
                .transactionId("TXN-20260314-483920")
                .build();
    }

    @Nested
    @DisplayName("createOrderFromCheckout")
    class CreateOrderFromCheckout {
        @Test
        @DisplayName("shouldBuildAndSaveOrderWithCorrectDataFromCheckout")
        void shouldBuildAndSaveOrderWithCorrectDataFromCheckout() {
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            doNothing().when(inventoryService).reduceInventory(any());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any(Order.class))).thenReturn(orderDTO);

                OrderDTO result = orderService.createOrderFromCheckout(checkout, successfulPayment);

                assertThat(result).isNotNull();
                assertThat(result.getOrderNumber()).isEqualTo("ORD-20260314-483920");

                ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(orderCaptor.capture());

                Order captured = orderCaptor.getValue();
                assertThat(captured.getCustomerInfo()).isEqualTo(user);
                assertThat(captured.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
                assertThat(captured.getShippingMethod()).isEqualTo(DeliverMethod.STANDARD.name());
                assertThat(captured.getSubtotal()).isEqualTo(99.98);
                assertThat(captured.getDiscount()).isEqualTo(0);
                // Order now carries the checkout's already-priced shipping (STANDARD = R0) and total.
                assertThat(captured.getShippingCost()).isEqualTo(0.0);
                assertThat(captured.getTotalAmount()).isEqualTo(114.98, org.assertj.core.data.Offset.offset(0.001));
            }
        }

        @Test
        @DisplayName("shouldAddInitialStatusHistoryEntryWithOrderCreatedNote")
        void shouldAddInitialStatusHistoryEntryWithOrderCreatedNote() {
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            doNothing().when(inventoryService).reduceInventory(any());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any(Order.class))).thenReturn(orderDTO);

                orderService.createOrderFromCheckout(checkout, successfulPayment);

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());

                List<OrderStatusHistory> history = captor.getValue().getStatusHistory();
                assertThat(history).hasSize(1);
                assertThat(history.get(0).getNotes()).isEqualTo("Order created");
            }
        }

        @Test
        @DisplayName("shouldReduceInventoryAfterOrderIsSaved")
        void shouldReduceInventoryAfterOrderIsSaved() {
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            doNothing().when(inventoryService).reduceInventory(any());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any(Order.class))).thenReturn(orderDTO);

                orderService.createOrderFromCheckout(checkout, successfulPayment);

                var inOrder = inOrder(orderRepository, inventoryService);
                inOrder.verify(orderRepository).save(any(Order.class));
                inOrder.verify(inventoryService).reduceInventory(checkout.getItems());
            }
        }

        @Test
        @DisplayName("shouldGenerateUniqueOrderNumber")
        void shouldGenerateUniqueOrderNumber() {
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            doNothing().when(inventoryService).reduceInventory(any());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any(Order.class))).thenReturn(orderDTO);

                orderService.createOrderFromCheckout(checkout, successfulPayment);
                orderService.createOrderFromCheckout(checkout, successfulPayment);

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository, times(2)).save(captor.capture());

                List<Order> savedOrders = captor.getAllValues();
                String orderNumber1 = savedOrders.get(0).getOrderNumber();
                String orderNumber2 = savedOrders.get(1).getOrderNumber();

                assertThat(orderNumber1).startsWith("ORD-");
                assertThat(orderNumber2).startsWith("ORD-");
            }
        }

        @ParameterizedTest(name = "shouldCalculateShippingCost_{0}")
        @EnumSource(DeliverMethod.class)
        @DisplayName("shouldCalculateCorrectShippingCostForEachDeliverMethod")
        void shouldCalculateCorrectShippingCostForEachDeliverMethod(DeliverMethod deliverMethod) {
            // recalculateTotals (in CheckoutService) prices shipping off the enum and
            // stores it on the checkout; the order simply carries that value through.
            checkout.setShippingMethod(deliverMethod);
            checkout.setShippingCost(deliverMethod.getPrice());
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            doNothing().when(inventoryService).reduceInventory(any());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any(Order.class))).thenReturn(orderDTO);

                orderService.createOrderFromCheckout(checkout, successfulPayment);

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getShippingCost()).isEqualTo(deliverMethod.getPrice());
            }
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {
        @Test
        @DisplayName("shouldReturnOrderDTOWhenOrderExists")
        void shouldReturnOrderDTOWhenOrderExists() {
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                OrderDTO result = orderService.getOrderById(orderId);

                assertThat(result).isNotNull();
                assertThat(result.getOrderNumber()).isEqualTo("ORD-20260314-483920");
            }
        }

        @Test
        @DisplayName("shouldThrowOrderExceptionWhenOrderNotFound")
        void shouldThrowOrderExceptionWhenOrderNotFound() {
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("getOrderByIdAndCustomerId")
    class GetOrderByIdAndCustomerId {
        @Test
        @DisplayName("shouldQueryRepositoryWithOrderIdThenCustomerId")
        void shouldQueryRepositoryWithOrderIdThenCustomerId() {
            // Repo signature is findByIdAndCustomerInfoIdAndDeletedFalse(orderId, customerId) — order matters.
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                OrderDTO result = orderService.getOrderByIdAndCustomerId(userId, orderId);

                assertThat(result).isNotNull();
                verify(orderRepository).findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId);
            }
        }

        @Test
        @DisplayName("shouldThrowOrderExceptionWhenOrderNotOwnedOrMissing")
        void shouldThrowOrderExceptionWhenOrderNotOwnedOrMissing() {
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderByIdAndCustomerId(userId, orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("getOrderByOrderNumber")
    class GetOrderByOrderNumber {
        @Test
        @DisplayName("shouldReturnOrderDTOWhenOrderNumberExists")
        void shouldReturnOrderDTOWhenOrderNumberExists() {
            when(orderRepository.findByOrderNumberAndDeletedFalse("ORD-20260314-483920")).thenReturn(Optional.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                OrderDTO result = orderService.getOrderByOrderNumber("ORD-20260314-483920");

                assertThat(result).isNotNull();
                assertThat(result.getOrderNumber()).isEqualTo("ORD-20260314-483920");
            }
        }

        @Test
        @DisplayName("shouldThrowOrderExceptionWhenOrderNumberNotFound")
        void shouldThrowOrderExceptionWhenOrderNumberNotFound() {
            when(orderRepository.findByOrderNumberAndDeletedFalse(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderByOrderNumber("ORD-INVALID"))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("getUserOrders")
    class GetUserOrders {
        @Test
        @DisplayName("shouldReturnOrderListWhenUserHasOrders")
        void shouldReturnOrderListWhenUserHasOrders() {
            when(orderRepository.findByCustomerInfoIdAndDeletedFalseOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                List<OrderDTO> results = orderService.getUserOrders(userId);

                assertThat(results).hasSize(1);
                assertThat(results.get(0).getOrderNumber()).isEqualTo("ORD-20260314-483920");
            }
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenUserHasNoOrders")
        void shouldReturnEmptyListWhenUserHasNoOrders() {
            when(orderRepository.findByCustomerInfoIdAndDeletedFalseOrderByCreatedAtDesc(userId)).thenReturn(List.of());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                List<OrderDTO> results = orderService.getUserOrders(userId);

                assertThat(results).isEmpty();
                mapperMock.verifyNoInteractions();
            }
        }
    }

    @Test
    void getUserOrdersByStatus() {
    }

    @Nested
    @DisplayName("getAllOrders")
    class GetAllOrders {
        @Test
        @DisplayName("shouldReturnAllOrdersWhenOrdersExist")
        void shouldReturnAllOrdersWhenOrdersExist() {
            when(orderRepository.findByDeletedFalse()).thenReturn(List.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                List<OrderDTO> results = orderService.getAllOrders();

                assertThat(results).hasSize(1);
            }
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenNoOrdersExist")
        void shouldReturnEmptyListWhenNoOrdersExist() {
            when(orderRepository.findByDeletedFalse()).thenReturn(List.of());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                List<OrderDTO> results = orderService.getAllOrders();

                assertThat(results).isEmpty();
                mapperMock.verifyNoInteractions();
            }
        }
    }

    @Nested
    @DisplayName("getOrdersByStatus")
    class GetOrdersByStatus {
        @Test
        @DisplayName("shouldReturnOrdersMatchingStatus")
        void shouldReturnOrdersMatchingStatus() {
            when(orderRepository.findByOrderStatusAndDeletedFalse(OrderStatus.CONFIRMED)).thenReturn(List.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                List<OrderDTO> results = orderService.getOrdersByStatus(OrderStatus.CONFIRMED);

                assertThat(results).hasSize(1);
            }
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenNoOrdersMatchStatus")
        void shouldReturnEmptyListWhenNoOrdersMatchStatus() {
            when(orderRepository.findByOrderStatusAndDeletedFalse(OrderStatus.CANCELLED)).thenReturn(List.of());

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                List<OrderDTO> results = orderService.getOrdersByStatus(OrderStatus.CANCELLED);

                assertThat(results).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatus {
        @Test
        @DisplayName("shouldThrowWhenOrderNotFound")
        void shouldThrowWhenOrderNotFound() {
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenTryingToUpdateCancelledOrder")
        void shouldThrowWhenTryingToUpdateCancelledOrder() {
            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("cancelled or refunded");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenTryingToUpdateRefundedOrder")
        void shouldThrowWhenTryingToUpdateRefundedOrder() {
            savedOrder.setOrderStatus(OrderStatus.REFUNDED);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("cancelled or refunded");
        }

        @Test
        @DisplayName("shouldThrowWhenTryingToSetDeliveredOrderToAnythingOtherThanRefunded")
        void shouldThrowWhenTryingToSetDeliveredOrderToAnythingOtherThanRefunded() {
            savedOrder.setOrderStatus(OrderStatus.DELIVERED);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("only be refunded");
        }

        @Test
        @DisplayName("shouldAllowDeliveredOrderToBeRefunded")
        void shouldAllowDeliveredOrderToBeRefunded() {
            savedOrder.setOrderStatus(OrderStatus.DELIVERED);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.REFUNDED, "Customer returned items");

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
            }
        }

        @Test
        @DisplayName("shouldUpdateStatusAndAddHistoryEntryWithCustomNotes")
        void shouldUpdateStatusAndAddHistoryEntryWithCustomNotes() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, "Order picked and packed");

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());

                Order updated = captor.getValue();
                assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
                assertThat(updated.getStatusHistory()).hasSize(1);
                assertThat(updated.getStatusHistory().get(0).getNotes()).isEqualTo("Order picked and packed");
            }
        }

        @Test
        @DisplayName("shouldAutoGenerateNotesWhenNullNotesProvided")
        void shouldAutoGenerateNotesWhenNullNotesProvided() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, null);

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());

                String autoNotes = captor.getValue().getStatusHistory().get(0).getNotes();
                assertThat(autoNotes).contains("CONFIRMED");
                assertThat(autoNotes).contains("PROCESSING");
            }
        }

        @Test
        @DisplayName("shouldSetShippedDateWhenStatusIsUpdatedToShipped")
        void shouldSetShippedDateWhenStatusIsUpdatedToShipped() {
            savedOrder.setOrderStatus(OrderStatus.PROCESSING);
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED, "Dispatched via DHL");

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getShippedDate()).isNotNull();
            }
        }

        @Test
        @DisplayName("shouldSetDeliveredDateWhenStatusIsUpdatedToDelivered")
        void shouldSetDeliveredDateWhenStatusIsUpdatedToDelivered() {
            savedOrder.setOrderStatus(OrderStatus.SHIPPED);
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED, "Delivered and signed for");

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getDeliveredDate()).isNotNull();
                assertThat(captor.getValue().getEstimatedDeliveryDate()).isNull();
            }
        }

        @Test
        @DisplayName("shouldInitialiseStatusHistoryListWhenItIsNull")
        void shouldInitialiseStatusHistoryListWhenItIsNull() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setStatusHistory(null);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, null);

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getStatusHistory()).isNotNull().hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("adjustOrderPricing")
    class AdjustOrderPricing {

        @Test
        @DisplayName("shouldRecalculateTotalApplyDiscountRecordHistoryAndEmail")
        void shouldRecalculateTotalApplyDiscountRecordHistoryAndEmail() {
            // buildOrder: subtotal 99.98, tax 15.0, discount 0, shipping 50.0 -> total 164.98
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.adjustOrderPricing(orderId, 20.0, null, "Goodwill discount");

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                Order saved = captor.getValue();
                // (99.98 - 20.0) + 15.0 + 50.0 = 144.98
                assertThat(saved.getDiscount()).isEqualTo(20.0);
                assertThat(saved.getShippingCost()).isEqualTo(50.0);
                assertThat(saved.getTotalAmount()).isEqualTo(144.98);
                assertThat(saved.getStatusHistory()).hasSize(1);
                assertThat(saved.getStatusHistory().get(0).getNotes()).contains("Goodwill discount");

                verify(emailService).sendOrderPriceAdjustmentEmail(eq(user), eq(orderDTO), eq("Goodwill discount"));
            }
        }

        @Test
        @DisplayName("shouldThrowWhenOrderNotFound")
        void shouldThrowWhenOrderNotFound() {
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.adjustOrderPricing(orderId, 10.0, null, "x"))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldRejectAdjustmentForCancelledOrder")
        void shouldRejectAdjustmentForCancelledOrder() {
            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() -> orderService.adjustOrderPricing(orderId, 10.0, null, "x"))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Cannot adjust pricing");
            verify(orderRepository, never()).save(any());
            verify(emailService, never()).sendOrderPriceAdjustmentEmail(any(), any(), any());
        }

        @Test
        @DisplayName("shouldRejectAdjustmentThatMakesTotalNegative")
        void shouldRejectAdjustmentThatMakesTotalNegative() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() -> orderService.adjustOrderPricing(orderId, 1000.0, null, "too much"))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("negative");
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("rebookOrder")
    class RebookOrder {

        private Order originalWith(Product product, int quantity) {
            Order original = TestDataBuilder.buildOrder(user); // shippingCost 50.0
            original.setOrderStatus(OrderStatus.DELIVERED);
            original.setOrderItems(List.of(OrderItems.builder()
                    .product(product).quantity(quantity).unitPrice(50.0).totalPrice(100.0).build()));
            return original;
        }

        @Test
        @DisplayName("shouldClonePendingOrderAtCurrentPricesReserveStockAndEmail")
        void shouldClonePendingOrderAtCurrentPricesReserveStockAndEmail() {
            Product product = TestDataBuilder.buildProduct(new ObjectId());
            product.setPrice(100.0); // price changed since the original order
            Order original = originalWith(product, 2);

            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(original));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.rebookOrder(orderId);

                verify(inventoryService).reduceInventory(any());

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                Order rebooked = captor.getValue();

                assertThat(rebooked.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
                assertThat(rebooked.getDiscount()).isZero();
                // Re-priced at current price 100.0 * 2 = 200 subtotal; tax 30; shipping 50; total 280.
                assertThat(rebooked.getSubtotal()).isEqualTo(200.0);
                assertThat(rebooked.getTax()).isEqualTo(30.0);
                assertThat(rebooked.getShippingCost()).isEqualTo(50.0);
                assertThat(rebooked.getTotalAmount()).isEqualTo(280.0);
                assertThat(rebooked.getOrderItems()).hasSize(1);
                assertThat(rebooked.getOrderItems().get(0).getUnitPrice()).isEqualTo(100.0);
                assertThat(rebooked.getOrderItems().get(0).getTotalPrice()).isEqualTo(200.0);
                assertThat(rebooked.getStatusHistory().get(0).getNotes()).contains("Rebooked from order");

                verify(emailService).sendOrderConfirmationEmail(eq(user), eq(orderDTO));
            }
        }

        @Test
        @DisplayName("shouldThrowWhenOriginalHasNoItems")
        void shouldThrowWhenOriginalHasNoItems() {
            Order original = TestDataBuilder.buildOrder(user);
            original.setOrderItems(List.of());
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(original));

            assertThatThrownBy(() -> orderService.rebookOrder(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("no items");
            verify(inventoryService, never()).reduceInventory(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenAProductIsUnavailable")
        void shouldThrowWhenAProductIsUnavailable() {
            Order original = TestDataBuilder.buildOrder(user);
            original.setOrderItems(List.of(OrderItems.builder().product(null).quantity(1).build()));
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(original));

            assertThatThrownBy(() -> orderService.rebookOrder(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("no longer available");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldNotSaveWhenInventoryInsufficient")
        void shouldNotSaveWhenInventoryInsufficient() {
            Product product = TestDataBuilder.buildProduct(new ObjectId());
            Order original = originalWith(product, 2);
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.of(original));
            doThrow(new InventoryException("400", "Insufficient inventory", 400))
                    .when(inventoryService).reduceInventory(any());

            assertThatThrownBy(() -> orderService.rebookOrder(orderId))
                    .isInstanceOf(InventoryException.class);
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenOrderNotFound")
        void shouldThrowWhenOrderNotFound() {
            when(orderRepository.findByIdAndDeletedFalse(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.rebookOrder(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("deleteStaleOrders")
    class DeleteStaleOrders {

        @Test
        @DisplayName("shouldSoftDeleteStaleCancelledOrdersAndReturnCount")
        void shouldSoftDeleteStaleCancelledOrdersAndReturnCount() {
            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            when(orderRepository.findByOrderStatusAndDeletedFalseAndCreatedAtBefore(eq(OrderStatus.CANCELLED), any()))
                    .thenReturn(List.of(savedOrder));

            int deleted = orderService.deleteStaleOrders(30);

            assertThat(deleted).isEqualTo(1);
            assertThat(savedOrder.isDeleted()).isTrue();
            assertThat(savedOrder.getDeletedAt()).isNotNull();
            verify(orderRepository).saveAll(any());
        }

        @Test
        @DisplayName("shouldReturnZeroAndNotSaveWhenNoStaleOrders")
        void shouldReturnZeroAndNotSaveWhenNoStaleOrders() {
            when(orderRepository.findByOrderStatusAndDeletedFalseAndCreatedAtBefore(eq(OrderStatus.CANCELLED), any()))
                    .thenReturn(List.of());

            int deleted = orderService.deleteStaleOrders(30);

            assertThat(deleted).isZero();
            verify(orderRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("shouldThrowWhenOlderThanDaysNegative")
        void shouldThrowWhenOlderThanDaysNegative() {
            assertThatThrownBy(() -> orderService.deleteStaleOrders(-1))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("zero or positive");
            verify(orderRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("getOrderStatistics")
    class GetOrderStatistics {

        private Order statsOrder(User customer, OrderStatus status, double total,
                                 List<OrderItems> items, LocalDateTime createdAt) {
            Order o = TestDataBuilder.buildOrder(customer);
            o.setOrderStatus(status);
            o.setTotalAmount(total);
            o.setOrderItems(items);
            o.setCreatedAt(createdAt);
            return o;
        }

        @Test
        @DisplayName("shouldComputeRevenueCountsCustomersTopSellersAndTrend")
        void shouldComputeRevenueCountsCustomersTopSellersAndTrend() {
            User customerA = TestDataBuilder.buildUser(new ObjectId());
            User customerB = TestDataBuilder.buildUser(new ObjectId());
            User customerC = TestDataBuilder.buildUser(new ObjectId());

            Product productX = TestDataBuilder.buildProduct(new ObjectId());
            productX.setTitle("Product X");
            Product productY = TestDataBuilder.buildProduct(new ObjectId());
            productY.setTitle("Product Y");

            LocalDateTime day = LocalDateTime.of(2026, 11, 9, 10, 0);

            Order o1 = statsOrder(customerA, OrderStatus.CONFIRMED, 100.0,
                    List.of(OrderItems.builder().product(productX).quantity(2).totalPrice(100.0).build()), day);
            Order o2 = statsOrder(customerB, OrderStatus.DELIVERED, 200.0,
                    List.of(
                            OrderItems.builder().product(productX).quantity(1).totalPrice(50.0).build(),
                            OrderItems.builder().product(productY).quantity(2).totalPrice(150.0).build()), day);
            Order cancelled = statsOrder(customerA, OrderStatus.CANCELLED, 999.0, List.of(), day);
            Order refunded = statsOrder(customerC, OrderStatus.REFUNDED, 50.0, List.of(), day);

            when(orderRepository.findByDeletedFalse()).thenReturn(List.of(o1, o2, cancelled, refunded));

            OrderStatisticsDTO stats = orderService.getOrderStatistics();

            assertThat(stats.getTotalOrders()).isEqualTo(4);
            assertThat(stats.getConfirmedOrders()).isEqualTo(1);
            assertThat(stats.getDeliveredOrders()).isEqualTo(1);
            assertThat(stats.getCancelledOrders()).isEqualTo(1);
            assertThat(stats.getRefundedOrders()).isEqualTo(1);

            // Revenue only over CONFIRMED + DELIVERED: 100 + 200 = 300; AOV 150.
            assertThat(stats.getTotalRevenue()).isEqualTo(300.0);
            assertThat(stats.getAverageOrderValue()).isEqualTo(150.0);

            // Distinct customers across all non-deleted orders: A, B, C.
            assertThat(stats.getTotalCustomers()).isEqualTo(3);

            // Top sellers from revenue orders: X qty 3, Y qty 2.
            assertThat(stats.getTopSellingProducts()).hasSize(2);
            assertThat(stats.getTopSellingProducts().get(0).getProductId())
                    .isEqualTo(productX.getId().toHexString());
            assertThat(stats.getTopSellingProducts().get(0).getQuantitySold()).isEqualTo(3);
            assertThat(stats.getTopSellingProducts().get(1).getQuantitySold()).isEqualTo(2);

            // Both revenue orders on the same day -> one bucket.
            assertThat(stats.getSalesByDay()).hasSize(1);
            assertThat(stats.getSalesByDay().get(0).getDate()).isEqualTo("2026-11-09");
            assertThat(stats.getSalesByDay().get(0).getOrderCount()).isEqualTo(2);
            assertThat(stats.getSalesByDay().get(0).getRevenue()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("shouldReturnZeroedStatsWhenNoOrders")
        void shouldReturnZeroedStatsWhenNoOrders() {
            when(orderRepository.findByDeletedFalse()).thenReturn(List.of());

            OrderStatisticsDTO stats = orderService.getOrderStatistics();

            assertThat(stats.getTotalOrders()).isZero();
            assertThat(stats.getTotalRevenue()).isZero();
            assertThat(stats.getAverageOrderValue()).isZero();
            assertThat(stats.getTotalCustomers()).isZero();
            assertThat(stats.getTopSellingProducts()).isEmpty();
            assertThat(stats.getSalesByDay()).isEmpty();
        }

        @Test
        @DisplayName("shouldHandleNullProductAndNullStatusGracefully")
        void shouldHandleNullProductAndNullStatusGracefully() {
            User customer = TestDataBuilder.buildUser(new ObjectId());
            LocalDateTime day = LocalDateTime.of(2026, 11, 9, 10, 0);

            // Revenue order with an item whose product is null (e.g. product later deleted).
            Order withNullProductItem = statsOrder(customer, OrderStatus.DELIVERED, 80.0,
                    List.of(OrderItems.builder().product(null).quantity(5).totalPrice(80.0).build()), day);
            // Order with a null status must not break counting.
            Order nullStatus = statsOrder(customer, OrderStatus.CONFIRMED, 20.0, List.of(), day);
            nullStatus.setOrderStatus(null);

            when(orderRepository.findByDeletedFalse()).thenReturn(List.of(withNullProductItem, nullStatus));

            OrderStatisticsDTO stats = orderService.getOrderStatistics();

            assertThat(stats.getTotalOrders()).isEqualTo(2);
            assertThat(stats.getDeliveredOrders()).isEqualTo(1);
            // Null-product item is skipped -> no top sellers.
            assertThat(stats.getTopSellingProducts()).isEmpty();
            // Revenue still counts the delivered order total.
            assertThat(stats.getTotalRevenue()).isEqualTo(80.0);
        }
    }

    @Nested
    @DisplayName("trackOrder")
    class TrackOrder {
        @Test
        @DisplayName("shouldReturnTrackingViewForOwnedOrder")
        void shouldReturnTrackingViewForOwnedOrder() {
            savedOrder.setStatusHistory(new ArrayList<>(List.of(
                    OrderStatusHistory.builder().status(OrderStatus.CONFIRMED)
                            .timestamp(DateUtil.now()).notes("Order created").build())));
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.of(savedOrder));

            savedOrder.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));

            OrderTrackingDTO tracking = orderService.trackOrder(userId, orderId);

            assertThat(tracking).isNotNull();
            assertThat(tracking.getOrderNumber()).isEqualTo("ORD-20260314-483920");
            assertThat(tracking.getCurrentStatus()).isEqualTo("CONFIRMED");
            assertThat(tracking.getHistory()).hasSize(1);
            assertThat(tracking.getHistory().get(0).getStatus()).isEqualTo("CONFIRMED");
            // CONFIRMED and within the window → cancellable.
            assertThat(tracking.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("shouldReportNotCancellableOnceShipped")
        void shouldReportNotCancellableOnceShipped() {
            savedOrder.setOrderStatus(OrderStatus.SHIPPED);
            savedOrder.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.of(savedOrder));

            OrderTrackingDTO tracking = orderService.trackOrder(userId, orderId);

            assertThat(tracking.getCurrentStatus()).isEqualTo("SHIPPED");
            assertThat(tracking.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("shouldThrowWhenOrderNotOwnedOrMissing")
        void shouldThrowWhenOrderNotOwnedOrMissing() {
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.trackOrder(userId, orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {
        @Test
        @DisplayName("shouldCancelRestoreInventoryAppendHistoryAndEmailWhenWithinWindow")
        void shouldCancelRestoreInventoryAppendHistoryAndEmailWhenWithinWindow() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderDTO result = orderService.cancelOrder(userId, orderId, "Changed my mind");

            assertThat(result).isNotNull();

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order cancelled = captor.getValue();
            assertThat(cancelled.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(cancelled.getStatusHistory()).hasSize(1);
            assertThat(cancelled.getStatusHistory().get(0).getNotes()).contains("Changed my mind");

            verify(inventoryService).restoreInventory(savedOrder.getOrderItems());
            verify(emailService).sendOrderStatusUpdateEmail(eq(user), any(OrderDTO.class), eq(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("shouldThrowWhenOrderAlreadyShipped")
        void shouldThrowWhenOrderAlreadyShipped() {
            savedOrder.setOrderStatus(OrderStatus.SHIPPED);
            savedOrder.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(userId, orderId, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("cannot be cancelled");

            verify(orderRepository, never()).save(any());
            verify(inventoryService, never()).restoreInventory(any());
        }

        @Test
        @DisplayName("shouldThrowWhenCancellationWindowHasPassed")
        void shouldThrowWhenCancellationWindowHasPassed() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setCreatedAt(java.time.LocalDateTime.now().minusHours(100));
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId))
                    .thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(userId, orderId, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("cancellation window");

            verify(orderRepository, never()).save(any());
            verify(inventoryService, never()).restoreInventory(any());
        }

        @Test
        @DisplayName("shouldThrowWhenOrderNotOwnedOrMissing")
        void shouldThrowWhenOrderNotOwnedOrMissing() {
            when(orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(userId, orderId, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("Order not found");
        }
    }
}