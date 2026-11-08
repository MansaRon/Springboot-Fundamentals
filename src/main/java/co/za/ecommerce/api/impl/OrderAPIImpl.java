package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.OrderAPI;
import co.za.ecommerce.config.AppConstants;
import co.za.ecommerce.dto.api.OrderDTOAllApiResource;
import co.za.ecommerce.dto.api.OrderDTOApiResource;
import co.za.ecommerce.dto.api.OrderDTOListApiResource;
import co.za.ecommerce.dto.api.OrderStatisticsApiResource;
import co.za.ecommerce.dto.api.OrderTrackingApiResource;
import co.za.ecommerce.dto.order.GetAllOrdersDTO;
import co.za.ecommerce.dto.order.OrderCancellationRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static co.za.ecommerce.utils.DateUtil.instantNow;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/order")
public class OrderAPIImpl extends API implements OrderAPI {

    // ADMIN only. By order id alone we can't tell who owns the order, so ownership
    // can't be enforced declaratively here — regular users fetch their own orders
    // via the ownership-scoped GET /{customerId}/{orderId} below.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTOApiResource> getOrderById(@PathVariable ObjectId orderId) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(instantNow())
                        .data(orderService.getOrderById(orderId))
                        .message("Order retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // ADMIN only — a non-admin caller is rejected with 403 before the method runs.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTOApiResource> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(instantNow())
                        .data(orderService.getOrderByOrderNumber(orderNumber))
                        .message("Order retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // CUSTOMER only — the caller's own order history (userId must match the principal).
    @PreAuthorize("#userId.toString() == authentication.principal.id.toString()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<OrderDTOListApiResource> getUserOrders(@PathVariable ObjectId userId) {
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(instantNow())
                        .data(orders)
                        .message(String.format("Retrieved %d orders.", orders.size()))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // CUSTOMER only — the caller filtering their own orders by status.
    @PreAuthorize("#userId.toString() == authentication.principal.id.toString()")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<OrderDTOListApiResource> getUserOrdersByStatus(
            @PathVariable ObjectId userId,
            @PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<OrderDTO> orders = orderService.getUserOrdersByStatus(userId, orderStatus);
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(instantNow())
                        .data(orders)
                        .message(String.format("Retrieved %d %s orders.", orders.size(), status))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // ADMIN only.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<OrderDTOAllApiResource> getAllOrders(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        GetAllOrdersDTO orders = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(
                OrderDTOAllApiResource.builder()
                        .timestamp(instantNow())
                        .data(orders)
                        .message(String.format("Retrieved %d total orders.", orders.getTotalElements()))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // ADMIN only.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<OrderDTOListApiResource> getOrdersByStatus(@PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<OrderDTO> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(
                OrderDTOListApiResource.builder()
                        .timestamp(instantNow())
                        .data(orders)
                        .message(String.format("Retrieved %d %s orders.", orders.size(), status))
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // ADMIN only — order status transitions.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTOApiResource> updateOrderStatus(
            @PathVariable ObjectId orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(instantNow())
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

    // ADMIN only.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/statistics")
    public ResponseEntity<OrderStatisticsApiResource> getOrderStatistics() {
        // TODO find a better means of handling these stats
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
                        .timestamp(instantNow())
                        .data(stats)
                        .message("Order statistics retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // CUSTOMER only — the path a regular user uses to fetch one of their own orders;
    // ownership is enforced declaratively via the customerId in the path.
    @PreAuthorize("#customerId.toString() == authentication.principal.id.toString()")
    @GetMapping("/{customerId}/{orderId}")
    @Override
    public ResponseEntity<OrderDTOApiResource> getOrderByIdAndCustomerId(
            @PathVariable ObjectId customerId,
            @PathVariable ObjectId orderId) {
        log.info("public ResponseEntity<OrderDTOApiResource> getOrderByIdAndCustomerId(@PathVariable ObjectId customerId, @PathVariable ObjectId orderId)");
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(instantNow())
                        .data(orderService.getOrderByIdAndCustomerId(customerId, orderId))
                        .message("Order retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // CUSTOMER only — tracking timeline for the caller's own order.
    @PreAuthorize("#customerId.toString() == authentication.principal.id.toString()")
    @GetMapping("/{customerId}/{orderId}/track")
    @Override
    public ResponseEntity<OrderTrackingApiResource> trackOrder(
            @PathVariable ObjectId customerId,
            @PathVariable ObjectId orderId) {
        log.info("ResponseEntity<OrderTrackingApiResource> trackOrder(@PathVariable ObjectId customerId, @PathVariable ObjectId orderId)");
        return ResponseEntity.ok(
                OrderTrackingApiResource.builder()
                        .timestamp(instantNow())
                        .data(orderService.trackOrder(customerId, orderId))
                        .message("Order tracking retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // CUSTOMER only — cancel the caller's own order (within the cancellation window).
    @PreAuthorize("#customerId.toString() == authentication.principal.id.toString()")
    @PostMapping("/{customerId}/{orderId}/cancel")
    @Override
    public ResponseEntity<OrderDTOApiResource> cancelOrder(
            @PathVariable ObjectId customerId,
            @PathVariable ObjectId orderId,
            @Valid @RequestBody(required = false) OrderCancellationRequest request) {
        log.info("ResponseEntity<OrderDTOApiResource> cancelOrder(@PathVariable ObjectId customerId, @PathVariable ObjectId orderId)");
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(instantNow())
                        .data(orderService.cancelOrder(customerId, orderId, reason))
                        .message("Order cancelled successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
