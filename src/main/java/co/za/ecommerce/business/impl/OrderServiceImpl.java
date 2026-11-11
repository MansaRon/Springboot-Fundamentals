package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.EmailService;
import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.DailySalesDTO;
import co.za.ecommerce.dto.order.GetAllOrdersDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderStatisticsDTO;
import co.za.ecommerce.dto.order.OrderTrackingDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.dto.order.TopProductDTO;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.mapper.OrderMapper;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.*;
import co.za.ecommerce.repository.OrderRepository;
import co.za.ecommerce.utils.DateUtil;
import co.za.ecommerce.utils.ValueUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
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

        Order order = orderRepository.findByOrderNumberAndDeletedFalse(orderNumber)
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
        return orderRepository.findByCustomerInfoIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getUserOrdersByStatus(ObjectId userId, OrderStatus status) {
        log.info("Fetching {} orders for user: {}", status, userId);

        return orderRepository.findByCustomerInfoIdAndOrderStatusAndDeletedFalse(userId, status)
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders (admin)");

        return orderRepository.findByDeletedFalse()
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GetAllOrdersDTO getAllOrders(int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Fetching all orders (admin) with pageNo: {}, pageSize: {}, sortBy: {}, sortDir: {}",
                pageNo, pageSize, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> orders = orderRepository.findByDeletedFalse(pageable);

        return buildGetAllOrdersDTO(orders);
    }

    private GetAllOrdersDTO buildGetAllOrdersDTO(Page<Order> page) {
        List<OrderDTO> orders = page.getContent()
                .stream()
                .map(OrderMapper::mapToOrderDTO)
                .toList();

        return GetAllOrdersDTO.builder()
                .orders(orders)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);

        return orderRepository.findByOrderStatusAndDeletedFalse(status)
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
    public OrderDTO adjustOrderPricing(ObjectId orderId, Double discount, Double shippingCost, String reason) {
        log.info("Adjusting pricing for order {}", orderId);

        Order order = findOrderById(orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot adjust pricing for a " + order.getOrderStatus() + " order.",
                    HttpStatus.BAD_REQUEST.value());
        }

        // null leaves the existing amount unchanged; an explicit 0 clears it.
        double appliedDiscount = discount != null ? discount : order.getDiscount();
        double appliedShipping = shippingCost != null ? shippingCost : order.getShippingCost();

        // Mirror the checkout total formula: (subtotal - discount) + tax + shipping.
        double newTotal = (order.getSubtotal() - appliedDiscount) + order.getTax() + appliedShipping;
        if (newTotal < 0) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Adjustment would make the order total negative.",
                    HttpStatus.BAD_REQUEST.value());
        }

        order.setDiscount(appliedDiscount);
        order.setShippingCost(appliedShipping);
        order.setTotalAmount(ValueUtil.round(newTotal, 2));
        order.setUpdatedAt(DateUtil.now());

        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .status(order.getOrderStatus())
                .timestamp(DateUtil.now())
                .notes("Pricing adjusted. Reason: " + reason)
                .updatedBy("ADMIN")
                .build());

        Order saved = orderRepository.save(order);
        log.info("Order {} pricing adjusted — new total {}", saved.getOrderNumber(), saved.getTotalAmount());

        OrderDTO dto = OrderMapper.mapToOrderDTO(saved);
        emailService.sendOrderPriceAdjustmentEmail(saved.getCustomerInfo(), dto, reason);
        return dto;
    }

    @Override
    public OrderDTO rebookOrder(ObjectId orderId) {
        log.info("Rebooking order {}", orderId);

        Order original = findOrderById(orderId);

        List<OrderItems> originalItems = original.getOrderItems();
        if (originalItems == null || originalItems.isEmpty()) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot rebook an order with no items.",
                    HttpStatus.BAD_REQUEST.value());
        }

        boolean hasMissingProduct = originalItems.stream()
                .anyMatch(item -> item.getProduct() == null || item.getProduct().getId() == null);
        if (hasMissingProduct) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot rebook: a product in this order is no longer available.",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Re-validate and reserve stock at current levels BEFORE creating the new order, so an
        // out-of-stock item aborts (InventoryException) without leaving an orphan order behind.
        List<CartItems> reservation = originalItems.stream()
                .map(item -> CartItems.builder()
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        inventoryService.reduceInventory(reservation);

        // Re-price every line at the product's current price.
        List<OrderItems> newItems = originalItems.stream()
                .map(item -> {
                    double unitPrice = item.getProduct().getPrice();
                    return OrderItems.builder()
                            .id(new ObjectId())
                            .createdAt(now())
                            .updatedAt(now())
                            .product(item.getProduct())
                            .quantity(item.getQuantity())
                            .unitPrice(unitPrice)
                            .totalPrice(ValueUtil.round(unitPrice * item.getQuantity(), 2))
                            .build();
                })
                .collect(Collectors.toList());

        double subtotal = newItems.stream().mapToDouble(OrderItems::getTotalPrice).sum();
        double tax = ValueUtil.vat(subtotal);
        double shippingCost = original.getShippingCost();
        double totalAmount = ValueUtil.round(subtotal + tax + shippingCost, 2);

        Order rebooked = Order.builder()
                .orderNumber(generateOrderNumber())
                .createdAt(now())
                .updatedAt(now())
                .customerInfo(original.getCustomerInfo())
                .orderItems(newItems)
                .shippingAddress(original.getShippingAddress())
                .billingAddress(original.getBillingAddress())
                .shippingMethod(original.getShippingMethod())
                .paymentMethod(original.getPaymentMethod())
                .paymentDetails(PaymentDetails.builder()
                        .paymentMethod(original.getPaymentMethod() != null ? original.getPaymentMethod().name() : null)
                        .paymentStatus(PaymentStatus.PENDING.name())
                        .paymentDate(now())
                        .build())
                .orderStatus(OrderStatus.PENDING)
                .subtotal(ValueUtil.round(subtotal, 2))
                .discount(0)
                .tax(ValueUtil.round(tax, 2))
                .shippingCost(shippingCost)
                .totalAmount(totalAmount)
                .statusHistory(new ArrayList<>(List.of(OrderStatusHistory.builder()
                        .status(OrderStatus.PENDING)
                        .timestamp(now())
                        .notes("Rebooked from order " + original.getOrderNumber())
                        .updatedBy("ADMIN")
                        .build())))
                .build();

        Order saved = orderRepository.save(rebooked);
        log.info("Order {} rebooked as {}", original.getOrderNumber(), saved.getOrderNumber());

        OrderDTO dto = OrderMapper.mapToOrderDTO(saved);
        emailService.sendOrderConfirmationEmail(saved.getCustomerInfo(), dto);
        return dto;
    }

    @Override
    public int deleteStaleOrders(int olderThanDays) {
        log.info("Soft-deleting stale CANCELLED orders older than {} days", olderThanDays);

        if (olderThanDays < 0) {
            throw new OrderException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "olderThanDays must be zero or positive.",
                    HttpStatus.BAD_REQUEST.value());
        }

        LocalDateTime cutoff = DateUtil.now().minusDays(olderThanDays);
        List<Order> staleOrders = orderRepository
                .findByOrderStatusAndDeletedFalseAndCreatedAtBefore(OrderStatus.CANCELLED, cutoff);

        if (staleOrders.isEmpty()) {
            log.info("No stale orders to delete.");
            return 0;
        }

        LocalDateTime deletedAt = DateUtil.now();
        staleOrders.forEach(order -> {
            order.setDeleted(true);
            order.setDeletedAt(deletedAt);
            order.setUpdatedAt(deletedAt);
        });
        orderRepository.saveAll(staleOrders);

        log.info("Soft-deleted {} stale CANCELLED orders", staleOrders.size());
        return staleOrders.size();
    }

    // Statuses that represent realised revenue — paid and not cancelled/refunded.
    private static final Set<OrderStatus> REVENUE_STATUSES = Set.of(
            OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    @Override
    public OrderStatisticsDTO getOrderStatistics() {
        log.info("Computing order statistics (admin)");

        List<Order> orders = orderRepository.findByDeletedFalse();

        Map<OrderStatus, Long> countByStatus = orders.stream()
                .filter(order -> order.getOrderStatus() != null)
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        List<Order> revenueOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != null && REVENUE_STATUSES.contains(order.getOrderStatus()))
                .collect(Collectors.toList());

        double totalRevenue = revenueOrders.stream().mapToDouble(Order::getTotalAmount).sum();
        double averageOrderValue = revenueOrders.isEmpty()
                ? 0.0
                : ValueUtil.round(totalRevenue / revenueOrders.size(), 2);

        long totalCustomers = orders.stream()
                .map(Order::getCustomerInfo)
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        return OrderStatisticsDTO.builder()
                .totalOrders(orders.size())
                .pendingOrders(statusCount(countByStatus, OrderStatus.PENDING))
                .confirmedOrders(statusCount(countByStatus, OrderStatus.CONFIRMED))
                .processingOrders(statusCount(countByStatus, OrderStatus.PROCESSING))
                .shippedOrders(statusCount(countByStatus, OrderStatus.SHIPPED))
                .deliveredOrders(statusCount(countByStatus, OrderStatus.DELIVERED))
                .cancelledOrders(statusCount(countByStatus, OrderStatus.CANCELLED))
                .refundedOrders(statusCount(countByStatus, OrderStatus.REFUNDED))
                .totalRevenue(ValueUtil.round(totalRevenue, 2))
                .averageOrderValue(averageOrderValue)
                .totalCustomers(totalCustomers)
                .topSellingProducts(topSellingProducts(revenueOrders))
                .salesByDay(salesByDay(revenueOrders))
                .build();
    }

    private int statusCount(Map<OrderStatus, Long> counts, OrderStatus status) {
        return counts.getOrDefault(status, 0L).intValue();
    }

    private List<TopProductDTO> topSellingProducts(List<Order> orders) {
        Map<String, List<OrderItems>> itemsByProduct = orders.stream()
                .map(Order::getOrderItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
                .collect(Collectors.groupingBy(item -> item.getProduct().getId().toHexString()));

        return itemsByProduct.values().stream()
                .map(items -> {
                    long quantity = items.stream().mapToLong(OrderItems::getQuantity).sum();
                    double revenue = items.stream().mapToDouble(OrderItems::getTotalPrice).sum();
                    Product product = items.get(0).getProduct();
                    return TopProductDTO.builder()
                            .productId(product.getId().toHexString())
                            .title(product.getTitle())
                            .quantitySold(quantity)
                            .revenue(ValueUtil.round(revenue, 2))
                            .build();
                })
                .sorted(Comparator.comparingLong(TopProductDTO::getQuantitySold).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<DailySalesDTO> salesByDay(List<Order> orders) {
        Map<LocalDate, List<Order>> byDay = orders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate()));

        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> DailySalesDTO.builder()
                        .date(entry.getKey().toString())
                        .orderCount(entry.getValue().size())
                        .revenue(ValueUtil.round(
                                entry.getValue().stream().mapToDouble(Order::getTotalAmount).sum(), 2))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderByIdAndCustomerId(ObjectId customerId, ObjectId orderId) {
        log.info("Fetching order by customerId and orderId: {} / {} ", orderId, customerId);
        Order order = orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, customerId)
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
        Order order = orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, customerId)
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

        Order order = orderRepository.findByIdAndCustomerInfoIdAndDeletedFalse(orderId, customerId)
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
        return orderRepository.findByIdAndDeletedFalse(orderId)
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
