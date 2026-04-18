package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.mapper.OrderMapper;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.order.*;
import co.za.ecommerce.repository.OrderRepository;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public OrderDTO createOrderFromCheckout(Checkout checkout, PaymentResultDTO paymentResultDTO) {
        log.info("Creating order from checkout: {}", checkout.getId());

        double shippingCost = calculateShippingCost(checkout.getShippingMethod());

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
                .totalAmount(checkout.getTotalAmount() + shippingCost)
                .paymentMethod(checkout.getPaymentMethod())
                .orderStatus(initialStatus)
                .statusHistory(new ArrayList<>(statusHistory))
                .transactionId(paymentResultDTO.getTransactionId())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {} ({})", savedOrder.getOrderNumber(), savedOrder.getId());

        inventoryService.reduceInventory(checkout.getItems());

        return OrderMapper.mapToOrderDTO(savedOrder);
    }

    @Override
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
    public List<OrderDTO> getUserOrders(ObjectId userId) {
        log.info("Fetching orders for user: {}", userId);

        return orderRepository.findByCustomerInfoId(userId)
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
            case DELIVERED -> order.setEstimatedDeliveryDate(DateUtil.now());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully");

        return OrderMapper.mapToOrderDTO(updatedOrder);
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

    private double calculateShippingCost(DeliverMethod deliverMethod) {
        return switch (deliverMethod) {
            case DHL -> 15.99;
            case FedEx -> 12.99;
            case Express -> 10.99;
            case FREE -> 0.0;
        };
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
