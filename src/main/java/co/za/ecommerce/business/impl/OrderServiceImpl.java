package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.EmailService;
import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderTrackingDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.mapper.OrderMapper;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.*;
import co.za.ecommerce.repository.OrderRepository;
import co.za.ecommerce.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;
import static co.za.ecommerce.utils.GenerateUtil.generateOrderNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final EmailService emailService;

    /**
     * How long after placement a customer may cancel their own order. Configurable
     * via {@code order.cancellation.window-hours} (defaults to 72h); the field
     * initialiser keeps unit tests working without Spring property injection.
     */
    @Value("${order.cancellation.window-hours:72}")
    private long cancellationWindowHours = 72;

    @Override
    @Transactional
    @CacheEvict(value = "user-orders", key = "#checkout.user.id")
    public OrderDTO createOrderFromCheckout(Checkout checkout, PaymentResultDTO paymentResultDTO) {
        log.info("Creating order from checkout: {}", checkout.getId());

        // Shipping was already priced and folded into the checkout total during
        // recalculateTotals — reuse it here rather than re-deriving (and re-adding) it.
        double shippingCost = checkout.getShippingCost();

        OrderStatus initialStatus = PaymentStatus.COMPLETED.equals(paymentResultDTO.getPaymentStatus())
                ? OrderStatus.CONFIRMED
                : OrderStatus.PENDING;

        List<OrderStatusHistory> statusHistory = List.of(OrderStatusHistory.builder()
                .status(initialStatus)
                .timestamp(DateUtil.now())
                .notes("Order created")
                .build());

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .createdAt(now())
                .updatedAt(now())
                .customerInfo(checkout.getUser())
                .orderItems(createOrderItems(checkout.getItems()))
                .paymentDetails(createPaymentDetails(checkout, paymentResultDTO))
                .shippingAddress(checkout.getShippingAddress())
                .billingAddress(checkout.getBillingAddress())
                .shippingMethod(checkout.getShippingMethod().name())
                .estimatedDeliveryDate(checkout.getEstimatedDeliveryDate())
                .subtotal(checkout.getSubtotal())
                .discount(checkout.getDiscount())
                .tax(checkout.getTax())
                .shippingCost(shippingCost)
                .totalAmount(checkout.getTotalAmount())
                .paymentMethod(checkout.getPaymentMethod())
                .orderStatus(initialStatus)
                .statusHistory(new ArrayList<>(statusHistory))
                .transactionId(paymentResultDTO.getTransactionId())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {} ({})", savedOrder.getOrderNumber(), savedOrder.getId());

        inventoryService.reduceInventory(checkout.getItems());

        OrderDTO orderDTO = OrderMapper.mapToOrderDTO(savedOrder);
        emailService.sendOrderConfirmationEmail(checkout.getUser(), orderDTO);
        return orderDTO;
    }

    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderDTO getOrderById(ObjectId orderId) {
        log.info("Fetching order by ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        return OrderMapper.mapToOrderDTO(order);
    }

    @Override
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order by order number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found",
                        HttpStatus.NOT_FOUND.value()
                ));
        return OrderMapper.mapToOrderDTO(order);
    }

    @Override
    @Cacheable(value = "user-orders", key = "#userId")
    public List<OrderDTO> getUserOrders(ObjectId userId) {
        log.info("Fetching orders for user: {}", userId);

        // Newest first so the frontend's order history / "latest order" shows the
        // just-placed order at the top rather than buried at the end.
        return orderRepository.findByCustomerInfoIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getUserOrdersByStatus(ObjectId userId, OrderStatus status) {
        log.info("Fetching {} orders for user: {}", status, userId);

        return orderRepository.findByCustomerInfoIdAndOrderStatus(userId, status)
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders (admin)");

        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);

        return orderRepository.findByOrderStatus(status)
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orders", key = "#orderId"),
            @CacheEvict(value = "user-orders", allEntries = true)
    })
    public OrderDTO updateOrderStatus(ObjectId orderId, OrderStatus newStatus, String notes) {
        log.info("Updating order {} status to: {}", orderId, newStatus);

        Order order = findOrderById(orderId);

        validateStatusTransition(order.getOrderStatus(), newStatus);

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(DateUtil.now());

        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }

        order.getStatusHistory().add(OrderStatusHistory.builder()
                .status(newStatus)
                .timestamp(DateUtil.now())
                .notes(notes != null ? notes : "Status updated from " + oldStatus + " to " + newStatus)
                .build());

        switch (newStatus) {
            case SHIPPED -> order.setShippedDate(DateUtil.now());
            case DELIVERED -> order.setDeliveredDate(DateUtil.now());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully");

        OrderDTO updatedDTO = OrderMapper.mapToOrderDTO(updatedOrder);
        if (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
            emailService.sendOrderStatusUpdateEmail(updatedOrder.getCustomerInfo(), updatedDTO, newStatus);
        }
        return updatedDTO;
    }

    @Override
    public OrderDTO getOrderByIdAndCustomerId(ObjectId customerId, ObjectId orderId) {
        log.info("Fetching order by customerId and orderId: {} / {} ", orderId, customerId);
        Order order = orderRepository.findByIdAndCustomerInfoId(orderId, customerId)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        return OrderMapper.mapToOrderDTO(order);
    }

    @Override
    public OrderTrackingDTO trackOrder(ObjectId customerId, ObjectId orderId) {
        log.info("Tracking order {} for customer {}", orderId, customerId);
        Order order = orderRepository.findByIdAndCustomerInfoId(orderId, customerId)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found",
                        HttpStatus.NOT_FOUND.value()
                ));
        OrderTrackingDTO tracking = OrderMapper.toTrackingDTO(order);
        // Compute cancellability server-side (status + window) so the frontend can
        // show/hide the Cancel action without a failed round-trip.
        tracking.setCancellable(isCancellable(order.getOrderStatus()) && !isCancellationWindowExpired(order));
        return tracking;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orders", key = "#orderId"),
            @CacheEvict(value = "user-orders", allEntries = true)
    })
    public OrderDTO cancelOrder(ObjectId customerId, ObjectId orderId, String reason) {
        log.info("Customer {} requested cancellation of order {}", customerId, orderId);

        Order order = orderRepository.findByIdAndCustomerInfoId(orderId, customerId)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        if (!isCancellable(order.getOrderStatus())) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Order cannot be cancelled while it is " + order.getOrderStatus() + ".",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (isCancellationWindowExpired(order)) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "The " + cancellationWindowHours + "-hour cancellation window for this order has passed. Please reach out to our support staff for assistance.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        OrderStatus previousStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(now());

        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }
        String note = (reason != null && !reason.isBlank())
                ? "Cancelled by customer: " + reason.trim()
                : "Cancelled by customer";
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .status(OrderStatus.CANCELLED)
                .timestamp(now())
                .notes(note)
                .updatedBy("customer:" + customerId.toHexString())
                .build());

        // Return the reserved stock to inventory on cancellation.
        if (order.getOrderItems() != null) {
            inventoryService.restoreInventory(order.getOrderItems());
        }

        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled (previous status: {})", orderId, previousStatus);

        OrderDTO dto = OrderMapper.mapToOrderDTO(saved);
        emailService.sendOrderStatusUpdateEmail(saved.getCustomerInfo(), dto, OrderStatus.CANCELLED);
        return dto;
    }

    private boolean isCancellable(OrderStatus status) {
        // Cancellable only before dispatch; SHIPPED/DELIVERED/CANCELLED/REFUNDED are terminal here.
        return status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING;
    }

    private boolean isCancellationWindowExpired(Order order) {
        if (order.getCreatedAt() == null) {
            return false;
        }
        long hoursSincePlaced = Duration.between(order.getCreatedAt(), now()).toHours();
        return hoursSincePlaced >= cancellationWindowHours;
    }

    private Order findOrderById(ObjectId orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found.",
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    private List<OrderItems> createOrderItems(List<CartItems> cartItems) {
        return cartItems.stream()
                .map(cartItem -> OrderItems.builder()
                        .id(new ObjectId())
                        .createdAt(DateUtil.now())
                        .updatedAt(DateUtil.now())
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .totalPrice(cartItem.getProductPrice())
                        .discount(cartItem.getDiscount())
                        .tax(cartItem.getTax())
                        .build())
                .collect(Collectors.toList());
    }

    private PaymentDetails createPaymentDetails(Checkout checkout, PaymentResultDTO paymentResult) {
        return PaymentDetails.builder()
                .paymentMethod(checkout.getPaymentMethod().toString())
                .paymentStatus(paymentResult.getPaymentStatus().toString())
                .paymentDate(DateUtil.now())
                .transactionId(paymentResult.getTransactionId())
                .build();
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.REFUNDED) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot update status of cancelled or refunded order.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (currentStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.REFUNDED) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Delivered orders can only be refunded.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }
}
