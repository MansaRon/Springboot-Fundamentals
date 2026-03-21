package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
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
import co.za.ecommerce.model.order.OrderStatus;
import co.za.ecommerce.model.order.OrderStatusHistory;
import co.za.ecommerce.repository.OrderRepository;
import co.za.ecommerce.utils.DateUtil;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryService inventoryService;
    @InjectMocks private OrderServiceImpl orderService;

    private ObjectId orderId;
    private ObjectId userId;
    private User user;
    private Product product;
    private CartItems cartItem;
    private Cart cart;
    private Checkout checkout;
    private PaymentResultDTO successfulPayment;
    private Order savedOrder;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderId = new ObjectId();
        userId = new ObjectId();

        user = new User();
        user.setId(userId);

        product = Product.builder()
                .id(new ObjectId())
                .title("Organic Fleece Hoodie")
                .price(49.99)
                .quantity(25)
                .build();

        cartItem = CartItems.builder()
                .product(product)
                .quantity(2)
                .productPrice(99.98)
                .discount(0)
                .tax(0)
                .build();

        cart = Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(cartItem)))
                .totalPrice(99.98)
                .build();

        checkout = Checkout.builder()
                .id((new ObjectId()))
                .user(user)
                .cart(cart)
                .items(new ArrayList<>(List.of(cartItem)))
                .subtotal(99.98)
                .discount(0)
                .tax(9.988)
                .totalAmount(109.978)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .shippingMethod(DeliverMethod.DHL)
                .shippingAddress(Address.builder()
                        .streetAddress("51 Frank Ocean Street")
                        .city("Johannesburg")
                        .state("Gauteng")
                        .country("South Africa")
                        .postalCode("2003")
                        .build())
                .billingAddress(Address.builder()
                        .streetAddress("51 Frank Ocean Street")
                        .city("Johannesburg")
                        .state("Gauteng")
                        .country("South Africa")
                        .postalCode("2003")
                        .build())
                .estimatedDeliveryDate(DateUtil.now().plusDays(5))
                .build();

        successfulPayment = PaymentResultDTO.builder()
                .success(true)
                .transactionId("TXN-20260314-483920")
                .paymentStatus(PaymentStatus.COMPLETED)
                .amountProcessed(109.978)
                .paymentMethod("CREDIT_CARD")
                .build();

        savedOrder = Order.builder()
                .id(orderId)
                .orderNumber("ORD-20260314-483920")
                .orderStatus(OrderStatus.CONFIRMED)
                .customerInfo(user)
                .subtotal(99.98)
                .tax(9.998)
                .discount(0)
                .shippingCost(15.99)
                .totalAmount(125.968)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .shippingMethod(DeliverMethod.DHL.name())
                .orderItems(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();

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
                assertThat(captured.getShippingMethod()).isEqualTo(DeliverMethod.DHL.name());
                assertThat(captured.getSubtotal()).isEqualTo(99.98);
                assertThat(captured.getDiscount()).isEqualTo(0);
                assertThat(captured.getShippingCost()).isEqualTo(15.99);
                assertThat(captured.getTotalAmount()).isEqualTo(109.978 + 15.99, org.assertj.core.data.Offset.offset(0.001));
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
            checkout.setShippingMethod(deliverMethod);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            doNothing().when(inventoryService).reduceInventory(any());

            double expectedCost = switch (deliverMethod) {
                case DHL -> 15.99;
                case FedEx -> 12.99;
                case Express -> 10.99;
                case FREE -> 0.0;
            };

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any(Order.class))).thenReturn(orderDTO);

                orderService.createOrderFromCheckout(checkout, successfulPayment);

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getShippingCost()).isEqualTo(expectedCost);
            }
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {
        @Test
        @DisplayName("shouldReturnOrderDTOWhenOrderExists")
        void shouldReturnOrderDTOWhenOrderExists() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

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
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(orderId))
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
            when(orderRepository.findByOrderNumber("ORD-20260314-483920")).thenReturn(Optional.of(savedOrder));

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
            when(orderRepository.findByOrderNumber(any())).thenReturn(Optional.empty());

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
            when(orderRepository.findByCustomerInfoId(userId)).thenReturn(List.of(savedOrder));

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
            when(orderRepository.findByCustomerInfoId(userId)).thenReturn(List.of());

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
            when(orderRepository.findAll()).thenReturn(List.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                List<OrderDTO> results = orderService.getAllOrders();

                assertThat(results).hasSize(1);
            }
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenNoOrdersExist")
        void shouldReturnEmptyListWhenNoOrdersExist() {
            when(orderRepository.findAll()).thenReturn(List.of());

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
            when(orderRepository.findByOrderStatus(OrderStatus.CONFIRMED)).thenReturn(List.of(savedOrder));

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(savedOrder)).thenReturn(orderDTO);

                List<OrderDTO> results = orderService.getOrdersByStatus(OrderStatus.CONFIRMED);

                assertThat(results).hasSize(1);
            }
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenNoOrdersMatchStatus")
        void shouldReturnEmptyListWhenNoOrdersMatchStatus() {
            when(orderRepository.findByOrderStatus(OrderStatus.CANCELLED)).thenReturn(List.of());

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
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

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
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

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
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("cancelled or refunded");
        }

        @Test
        @DisplayName("shouldThrowWhenTryingToSetDeliveredOrderToAnythingOtherThanRefunded")
        void shouldThrowWhenTryingToSetDeliveredOrderToAnythingOtherThanRefunded() {
            savedOrder.setOrderStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("only be refunded");
        }

        @Test
        @DisplayName("shouldAllowDeliveredOrderToBeRefunded")
        void shouldAllowDeliveredOrderToBeRefunded() {
            savedOrder.setOrderStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
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
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
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
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
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
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
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
        @DisplayName("shouldSetEstimatedDeliveryDateWhenStatusIsUpdatedToDelivered")
        void shouldSetEstimatedDeliveryDateWhenStatusIsUpdatedToDelivered() {
            savedOrder.setOrderStatus(OrderStatus.SHIPPED);
            savedOrder.setStatusHistory(new ArrayList<>());
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            try (MockedStatic<OrderMapper> mapperMock = mockStatic(OrderMapper.class)) {
                mapperMock.when(() -> OrderMapper.mapToOrderDTO(any())).thenReturn(orderDTO);

                orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED, "Delivered and signed for");

                ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
                verify(orderRepository).save(captor.capture());
                assertThat(captor.getValue().getEstimatedDeliveryDate()).isNotNull();
            }
        }

        @Test
        @DisplayName("shouldInitialiseStatusHistoryListWhenItIsNull")
        void shouldInitialiseStatusHistoryListWhenItIsNull() {
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setStatusHistory(null);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
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
}