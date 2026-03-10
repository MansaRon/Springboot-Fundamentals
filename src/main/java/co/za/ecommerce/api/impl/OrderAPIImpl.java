package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.OrderAPI;
import co.za.ecommerce.dto.api.OrderDTOApiResource;
import co.za.ecommerce.dto.api.OrderDTOListApiResource;
import co.za.ecommerce.dto.api.OrderStatisticsApiResource;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderStatisticsDTO;
import co.za.ecommerce.dto.order.OrderStatusUpdateRequest;
import co.za.ecommerce.model.order.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/order")
public class OrderAPIImpl extends API implements OrderAPI {

    /**
     * Get order by ID
     * GET /api/v1/orders/{orderId}
     *
     * Customer can view their own order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTOApiResource> getOrderById(@PathVariable ObjectId orderId) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orderService.getOrderById(orderId))
                        .message("Order retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Get order by order number
     * GET /api/v1/orders/number/{orderNumber}
     *
     * Allows customer to track order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTOApiResource> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orderService.getOrderByOrderNumber(orderNumber))
                        .message("Order retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Get all orders for a user
     * GET /api/v1/orders/user/{userId}
     *
     * Customer views their order history
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<OrderDTOListApiResource> getUserOrders(@PathVariable ObjectId userId) {
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orders)
                        .message(String.format("Retrieved %d orders.", orders.size()))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Get user orders by status
     * GET /api/v1/orders/user/{userId}/status/{status}
     *
     * Filter user's orders by status (e.g., PENDING, SHIPPED, DELIVERED)
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<OrderDTOListApiResource> getUserOrdersByStatus(
            @PathVariable ObjectId userId,
            @PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<OrderDTO> orders = orderService.getUserOrdersByStatus(userId, orderStatus);
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orders)
                        .message(String.format("Retrieved %d %s orders.", orders.size(), status))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Get all orders (Admin dashboard)
     * GET /api/v1/orders/admin/all
     *
     * Admin views all orders across all customers
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/all")
    public ResponseEntity<OrderDTOListApiResource> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orders)
                        .message(String.format("Retrieved %d total orders.", orders.size()))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Get orders by status (Admin)
     * GET /api/v1/orders/admin/status/{status}
     *
     * Admin filters orders by status
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<OrderDTOListApiResource> getOrdersByStatus(@PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<OrderDTO> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orders)
                        .message(String.format("Retrieved %d %s orders.", orders.size(), status))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Update order status (Admin)
     * PUT /api/v1/orders/{orderId}/status
     *
     * Admin updates order status (e.g., CONFIRMED → PROCESSING → SHIPPED → DELIVERED)
     */
    @Secured("ROLE_ADMIN")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTOApiResource> updateOrderStatus(
            @PathVariable ObjectId orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        OrderDTO updatedOrder = orderService.updateOrderStatus(
                orderId,
                request.getNewStatus(),
                request.getNotes()
        );

        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(updatedOrder)
                        .message("Order status updated to " + request.getNewStatus())
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Get order statistics (Admin dashboard)
     * GET /api/v1/orders/admin/statistics
     *
     * Returns order counts by status for dashboard
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/statistics")
    public ResponseEntity<OrderStatisticsApiResource> getOrderStatistics() {
        OrderStatisticsDTO stats = OrderStatisticsDTO.builder()
                .totalOrders(orderService.getAllOrders().size())
                .pendingOrders(orderService.getOrdersByStatus(OrderStatus.PENDING).size())
                .confirmedOrders(orderService.getOrdersByStatus(OrderStatus.CONFIRMED).size())
                .processingOrders(orderService.getOrdersByStatus(OrderStatus.PROCESSING).size())
                .shippedOrders(orderService.getOrdersByStatus(OrderStatus.SHIPPED).size())
                .deliveredOrders(orderService.getOrdersByStatus(OrderStatus.DELIVERED).size())
                .cancelledOrders(orderService.getOrdersByStatus(OrderStatus.CANCELLED).size())
                .build();

        return ResponseEntity.ok(
                OrderStatisticsApiResource.builder()
                        .timestamp(Instant.now())
                        .data(stats)
                        .message("Order statistics retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
