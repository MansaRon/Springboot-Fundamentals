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

    @Secured({"ROLE_ADMIN"})
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

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
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

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/user/{userId}")
    public ResponseEntity<OrderDTOListApiResource> getUserOrders(@PathVariable ObjectId userId) {
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orderService.getUserOrders(userId))
                        .message(String.format("Retrieved %d orders.", orderService.getUserOrders(userId).size()))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Secured({"ROLE_ADMIN"})
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

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/all")
    public ResponseEntity<OrderDTOListApiResource> getAllOrders() {
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orderService.getAllOrders())
                        .message(String.format("Retrieved %d total orders.", orderService.getAllOrders().size()))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

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

    @Secured("ROLE_ADMIN")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTOApiResource> updateOrderStatus(
            @PathVariable ObjectId orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(orderService.updateOrderStatus(
                                orderId,
                                request.getNewStatus(),
                                request.getNotes()
                        ))
                        .message("Order status updated to " + request.getNewStatus())
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

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
