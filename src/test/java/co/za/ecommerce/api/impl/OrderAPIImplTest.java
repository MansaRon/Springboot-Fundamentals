package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.order.GetAllOrdersDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderStatusHistoryDTO;
import co.za.ecommerce.dto.order.OrderTrackingDTO;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.order.OrderStatus;
import org.springframework.http.HttpStatus;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("OrderAPI Controller Tests")
class OrderAPIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderService orderService;

    @MockBean private OTPService otpService;
    @MockBean private UserService userService;
    @MockBean private ProductService productService;
    @MockBean private CartService cartService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private WishlistService wishlistService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private LookupService lookupService;

    private static final String ORDER_ID     = "507f1f77bcf86cd799439011";
    private static final String USER_ID      = "507f1f77bcf86cd799439022";
    private static final String ORDER_NUMBER = "ORD-20260314-483920";

    private OrderDTO orderDTO;
    private OrderDTO orderDTO2;

    @BeforeEach
    void setUp() {
        orderDTO = OrderDTO.builder()
                .id(ORDER_ID)
                .orderNumber(ORDER_NUMBER)
                .orderStatus(OrderStatus.CONFIRMED.name())
                .transactionId("TXN-20260314-8F4E2A")
                .subtotal(99.98)
                .tax(9.998)
                .shippingCost(15.99)
                .totalAmount(125.968)
                .build();

        orderDTO2 = OrderDTO.builder()
                .id("507f1f77bcf86cd799439099")
                .orderNumber("ORD-20260314-999999")
                .orderStatus(OrderStatus.PENDING.name())
                .totalAmount(59.99)
                .build();
    }

    // ADMIN-only endpoint. Ownership-scoped single-order fetch for a regular user is
    // covered by GET /{customerId}/{orderId} (see GetOrderByIdAndCustomerId below).
    @Nested
    @DisplayName("GET /api/v1/order/{orderId}")
    class GetOrderById {

        @Test
        @DisplayName("shouldReturn200WithOrderWhenIdExists")
        void shouldReturn200WithOrderWhenIdExists() throws Exception {
            when(orderService.getOrderById(eq(new ObjectId(ORDER_ID)))).thenReturn(orderDTO);

            mockMvc.perform(get("/api/v1/order/{orderId}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order retrieved successfully."))
                    .andExpect(jsonPath("$.data.orderNumber").value(ORDER_NUMBER))
                    .andExpect(jsonPath("$.data.orderStatus").value("CONFIRMED"))
                    .andExpect(jsonPath("$.data.transactionId").value("TXN-20260314-8F4E2A"))
                    .andExpect(jsonPath("$.data.totalAmount").value(125.968));
        }

        @Test
        @DisplayName("shouldPassCorrectOrderIdToService")
        void shouldPassCorrectOrderIdToService() throws Exception {
            when(orderService.getOrderById(any())).thenReturn(orderDTO);

            mockMvc.perform(get("/api/v1/order/{orderId}", ORDER_ID)).andExpect(status().isOk());

            verify(orderService).getOrderById(eq(new ObjectId(ORDER_ID)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/{customerId}/{orderId}")
    class GetOrderByIdAndCustomerId {

        @Test
        @DisplayName("shouldReturn200WithOrderForOwnerScopedLookup")
        void shouldReturn200WithOrderForOwnerScopedLookup() throws Exception {
            when(orderService.getOrderByIdAndCustomerId(
                    eq(new ObjectId(USER_ID)), eq(new ObjectId(ORDER_ID))))
                    .thenReturn(orderDTO);

            mockMvc.perform(get("/api/v1/order/{customerId}/{orderId}", USER_ID, ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order retrieved successfully."))
                    .andExpect(jsonPath("$.data.orderNumber").value(ORDER_NUMBER));

            verify(orderService).getOrderByIdAndCustomerId(
                    eq(new ObjectId(USER_ID)), eq(new ObjectId(ORDER_ID)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/number/{orderNumber}")
    class GetOrderByOrderNumber {

        @Test
        @DisplayName("shouldReturn200WithOrderWhenOrderNumberExists")
        void shouldReturn200WithOrderWhenOrderNumberExists() throws Exception {
            when(orderService.getOrderByOrderNumber(eq(ORDER_NUMBER))).thenReturn(orderDTO);

            mockMvc.perform(get("/api/v1/order/number/{orderNumber}", ORDER_NUMBER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order retrieved successfully."))
                    .andExpect(jsonPath("$.data.orderNumber").value(ORDER_NUMBER));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/user/{userId}")
    class GetUserOrders {

        @Test
        @DisplayName("shouldReturn200WithOrderListWhenUserHasOrders")
        void shouldReturn200WithOrderListWhenUserHasOrders() throws Exception {
            when(orderService.getUserOrders(eq(new ObjectId(USER_ID))))
                    .thenReturn(List.of(orderDTO, orderDTO2));

            mockMvc.perform(get("/api/v1/order/user/{userId}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    // Message uses the list size — "Retrieved 2 orders."
                    .andExpect(jsonPath("$.message").value("Retrieved 2 orders."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].orderNumber").value(ORDER_NUMBER));

            // The handler must query orders only once (message reuses the fetched list).
            verify(orderService, times(1)).getUserOrders(eq(new ObjectId(USER_ID)));
        }

        @Test
        @DisplayName("shouldReturn200WithEmptyListWhenUserHasNoOrders")
        void shouldReturn200WithEmptyListWhenUserHasNoOrders() throws Exception {
            when(orderService.getUserOrders(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/order/user/{userId}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Retrieved 0 orders."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/user/{userId}/status/{status}")
    class GetUserOrdersByStatus {

        @Test
        @DisplayName("shouldReturn200WithFilteredOrdersWhenValidStatusProvided")
        void shouldReturn200WithFilteredOrdersWhenValidStatusProvided() throws Exception {
            when(orderService.getUserOrdersByStatus(
                    eq(new ObjectId(USER_ID)), eq(OrderStatus.CONFIRMED)))
                    .thenReturn(List.of(orderDTO));

            mockMvc.perform(get("/api/v1/order/user/{userId}/status/{status}",
                            USER_ID, "CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Retrieved 1 CONFIRMED orders."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].orderStatus").value("CONFIRMED"));
        }

        @Test
        @DisplayName("shouldReturn200WithEmptyListWhenNoOrdersMatchStatus")
        void shouldReturn200WithEmptyListWhenNoOrdersMatchStatus() throws Exception {
            when(orderService.getUserOrdersByStatus(any(), eq(OrderStatus.CANCELLED)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/order/user/{userId}/status/{status}",
                            USER_ID, "CANCELLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Retrieved 0 CANCELLED orders."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/admin/all")
    class GetAllOrders {

        @Test
        @DisplayName("shouldReturn200WithAllOrdersWhenOrdersExist")
        void shouldReturn200WithAllOrdersWhenOrdersExist() throws Exception {
            GetAllOrdersDTO page = GetAllOrdersDTO.builder()
                    .orders(List.of(orderDTO, orderDTO2))
                    .pageNo(0)
                    .pageSize(10)
                    .totalElements(2)
                    .totalPages(1)
                    .last(true)
                    .build();
            when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

            mockMvc.perform(get("/api/v1/order/admin/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Retrieved 2 total orders."))
                    .andExpect(jsonPath("$.data.orders").isArray())
                    .andExpect(jsonPath("$.data.orders.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @DisplayName("shouldReturn200WithEmptyListWhenNoOrdersExist")
        void shouldReturn200WithEmptyListWhenNoOrdersExist() throws Exception {
            GetAllOrdersDTO page = GetAllOrdersDTO.builder()
                    .orders(List.of())
                    .pageNo(0)
                    .pageSize(10)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
            when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

            mockMvc.perform(get("/api/v1/order/admin/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Retrieved 0 total orders."))
                    .andExpect(jsonPath("$.data.orders").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/admin/status/{status}")
    class GetOrdersByStatus {

        @Test
        @DisplayName("shouldReturn200WithOrdersMatchingStatus")
        void shouldReturn200WithOrdersMatchingStatus() throws Exception {
            when(orderService.getOrdersByStatus(eq(OrderStatus.SHIPPED)))
                    .thenReturn(List.of(orderDTO));

            mockMvc.perform(get("/api/v1/order/admin/status/{status}", "SHIPPED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Retrieved 1 SHIPPED orders."))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/order/{orderId}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("shouldReturn200WithUpdatedOrderWhenStatusTransitionIsValid")
        void shouldReturn200WithUpdatedOrderWhenStatusTransitionIsValid() throws Exception {
            OrderDTO updatedOrder = OrderDTO.builder()
                    .id(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .orderStatus(OrderStatus.PROCESSING.name())
                    .totalAmount(125.968)
                    .build();

            when(orderService.updateOrderStatus(
                    eq(new ObjectId(ORDER_ID)),
                    eq(OrderStatus.PROCESSING),
                    eq("Order picked and packed.")))
                    .thenReturn(updatedOrder);

            mockMvc.perform(put("/api/v1/order/{orderId}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "newStatus": "PROCESSING",
                                      "notes": "Order picked and packed."
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order status updated to PROCESSING"))
                    .andExpect(jsonPath("$.data.orderStatus").value("PROCESSING"));
        }

        @Test
        @DisplayName("shouldReturn200WithNullNotesWhenNotesAreOmitted")
        void shouldReturn200WithNullNotesWhenNotesAreOmitted() throws Exception {
            when(orderService.updateOrderStatus(
                    eq(new ObjectId(ORDER_ID)),
                    eq(OrderStatus.PROCESSING),
                    isNull()))
                    .thenReturn(orderDTO);

            mockMvc.perform(put("/api/v1/order/{orderId}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "newStatus": "PROCESSING"
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/order/{orderId}/pricing")
    class AdjustOrderPricing {

        @Test
        @DisplayName("shouldReturn200WithAdjustedOrder")
        void shouldReturn200WithAdjustedOrder() throws Exception {
            OrderDTO adjusted = OrderDTO.builder()
                    .id(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .orderStatus(OrderStatus.CONFIRMED.name())
                    .discount(20.0)
                    .totalAmount(144.98)
                    .build();

            when(orderService.adjustOrderPricing(
                    eq(new ObjectId(ORDER_ID)),
                    eq(20.0),
                    isNull(),
                    eq("Goodwill discount")))
                    .thenReturn(adjusted);

            mockMvc.perform(patch("/api/v1/order/{orderId}/pricing", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "discount": 20.0,
                                      "reason": "Goodwill discount"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order pricing adjusted."))
                    .andExpect(jsonPath("$.data.totalAmount").value(144.98));
        }

        @Test
        @DisplayName("shouldReturn400WhenReasonMissing")
        void shouldReturn400WhenReasonMissing() throws Exception {
            mockMvc.perform(patch("/api/v1/order/{orderId}/pricing", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "discount": 20.0
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            verify(orderService, never()).adjustOrderPricing(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/order/admin/stale")
    class DeleteStaleOrders {

        @Test
        @DisplayName("shouldReturn200WithDeletedCount")
        void shouldReturn200WithDeletedCount() throws Exception {
            when(orderService.deleteStaleOrders(30)).thenReturn(5);

            mockMvc.perform(delete("/api/v1/order/admin/stale").param("olderThanDays", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Soft-deleted 5 stale order(s)."));
        }

        @Test
        @DisplayName("shouldDefaultTo30DaysWhenParamOmitted")
        void shouldDefaultTo30DaysWhenParamOmitted() throws Exception {
            when(orderService.deleteStaleOrders(30)).thenReturn(0);

            mockMvc.perform(delete("/api/v1/order/admin/stale"))
                    .andExpect(status().isOk());

            verify(orderService).deleteStaleOrders(30);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/admin/statistics")
    class GetOrderStatistics {

        @Test
        @DisplayName("shouldReturn200WithCorrectStatisticsWhenOrdersExist")
        void shouldReturn200WithCorrectStatisticsWhenOrdersExist() throws Exception {
            // Stub each service call the statistics endpoint makes
            when(orderService.getAllOrders()).thenReturn(List.of(orderDTO, orderDTO2));
            when(orderService.getOrdersByStatus(eq(OrderStatus.PENDING)))
                    .thenReturn(List.of(orderDTO2));
            when(orderService.getOrdersByStatus(eq(OrderStatus.CONFIRMED)))
                    .thenReturn(List.of(orderDTO));
            when(orderService.getOrdersByStatus(eq(OrderStatus.PROCESSING)))
                    .thenReturn(List.of());
            when(orderService.getOrdersByStatus(eq(OrderStatus.SHIPPED)))
                    .thenReturn(List.of());
            when(orderService.getOrdersByStatus(eq(OrderStatus.DELIVERED)))
                    .thenReturn(List.of());
            when(orderService.getOrdersByStatus(eq(OrderStatus.CANCELLED)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/order/admin/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Order statistics retrieved."))
                    .andExpect(jsonPath("$.data.totalOrders").value(2))
                    .andExpect(jsonPath("$.data.pendingOrders").value(1))
                    .andExpect(jsonPath("$.data.confirmedOrders").value(1))
                    .andExpect(jsonPath("$.data.processingOrders").value(0))
                    .andExpect(jsonPath("$.data.shippedOrders").value(0))
                    .andExpect(jsonPath("$.data.deliveredOrders").value(0))
                    .andExpect(jsonPath("$.data.cancelledOrders").value(0));
        }

        @Test
        @DisplayName("shouldReturn200WithAllZerosWhenNoOrdersExist")
        void shouldReturn200WithAllZerosWhenNoOrdersExist() throws Exception {
            when(orderService.getAllOrders()).thenReturn(List.of());
            when(orderService.getOrdersByStatus(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/order/admin/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalOrders").value(0))
                    .andExpect(jsonPath("$.data.pendingOrders").value(0))
                    .andExpect(jsonPath("$.data.confirmedOrders").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/order/{customerId}/{orderId}/track")
    class TrackOrder {
        @Test
        @DisplayName("shouldReturn200WithTrackingTimeline")
        void shouldReturn200WithTrackingTimeline() throws Exception {
            OrderStatusHistoryDTO entry = OrderStatusHistoryDTO.builder()
                    .status("CONFIRMED").notes("Order created").build();
            OrderTrackingDTO tracking = OrderTrackingDTO.builder()
                    .orderNumber(ORDER_NUMBER)
                    .currentStatus("CONFIRMED")
                    .shippingMethod("STANDARD")
                    .cancellable(true)
                    .history(List.of(entry))
                    .build();

            when(orderService.trackOrder(eq(new ObjectId(USER_ID)), eq(new ObjectId(ORDER_ID))))
                    .thenReturn(tracking);

            mockMvc.perform(get("/api/v1/order/{customerId}/{orderId}/track", USER_ID, ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Order tracking retrieved successfully."))
                    .andExpect(jsonPath("$.data.orderNumber").value(ORDER_NUMBER))
                    .andExpect(jsonPath("$.data.currentStatus").value("CONFIRMED"))
                    .andExpect(jsonPath("$.data.cancellable").value(true))
                    .andExpect(jsonPath("$.data.history[0].status").value("CONFIRMED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/order/{customerId}/{orderId}/cancel")
    class CancelOrder {
        @Test
        @DisplayName("shouldReturn200WithCancelledOrder")
        void shouldReturn200WithCancelledOrder() throws Exception {
            when(orderService.cancelOrder(eq(new ObjectId(USER_ID)), eq(new ObjectId(ORDER_ID)), any()))
                    .thenReturn(orderDTO);

            mockMvc.perform(post("/api/v1/order/{customerId}/{orderId}/cancel", USER_ID, ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"reason\":\"Changed my mind\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Order cancelled successfully."))
                    .andExpect(jsonPath("$.data.orderNumber").value(ORDER_NUMBER));
        }

        @Test
        @DisplayName("shouldReturn200WhenNoBodyProvided")
        void shouldReturn200WhenNoBodyProvided() throws Exception {
            when(orderService.cancelOrder(eq(new ObjectId(USER_ID)), eq(new ObjectId(ORDER_ID)), any()))
                    .thenReturn(orderDTO);

            mockMvc.perform(post("/api/v1/order/{customerId}/{orderId}/cancel", USER_ID, ORDER_ID))
                    .andExpect(status().isOk());

            verify(orderService).cancelOrder(eq(new ObjectId(USER_ID)), eq(new ObjectId(ORDER_ID)), any());
        }

        @Test
        @DisplayName("shouldReturn400WhenNotCancellable")
        void shouldReturn400WhenNotCancellable() throws Exception {
            when(orderService.cancelOrder(any(), any(), any()))
                    .thenThrow(new OrderException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "The 72-hour cancellation window for this order has passed.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(post("/api/v1/order/{customerId}/{orderId}/cancel", USER_ID, ORDER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("The 72-hour cancellation window for this order has passed."));
        }
    }
}