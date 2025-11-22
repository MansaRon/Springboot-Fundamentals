package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.model.order.OrderStatus;
import co.za.ecommerce.model.order.PaymentDetails;
import co.za.ecommerce.repository.CheckoutRepository;
import co.za.ecommerce.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.mapper.OrderMapper.mapToOrderDTO;
import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final CheckoutRepository checkoutRepository;

    /**
     * Create an order from a checkout after payment confirmation
     */
    @Override
    public Order createOrderFromCheckout(Checkout checkout, String transactionId) {
        log.info("Creating order from checkout: {}", checkout.getId());

        // 1. Create order items
        List<OrderItems> orderItems = createOrderItems(checkout.getItems());

        // 2. Create payment details
        PaymentDetails paymentDetails = createPaymentDetails(checkout, transactionId);

        // 3. Build order
        Order order = new Order();
        order.setCreatedAt(now());
        order.setUpdatedAt(now());
        order.setCustomerInfo(checkout.getUser());
        order.setOrderItems(orderItems);
        order.setPaymentDetails(paymentDetails);
        order.setShippingAddress(checkout.getShippingAddress());
        order.setBillingAddress(checkout.getBillingAddress());
        order.setShippingMethod(checkout.getShippingMethod().name());
        order.setEstimatedDeliveryDate(checkout.getEstimatedDeliveryDate());
        order.setSubtotal(checkout.getSubtotal());
        order.setDiscount(checkout.getDiscount());
        order.setTax(checkout.getTax());
        order.setTotalAmount(checkout.getTotalAmount());
        order.setTransactionId(transactionId);
        order.setPaymentMethod(checkout.getPaymentMethod());

        // Set initial order status based on payment method
        if (PaymentMethod.CASH_ON_DELIVERY.equals(checkout.getPaymentMethod())) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        } else {
            order.setOrderStatus(OrderStatus.PAID);
        }

        // 4. Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // 5. Update inventory
        inventoryService.reduceInventory(checkout.getItems());

        // 6. Clear cart
        cartService.clearCart(checkout.getCart());

        return savedOrder;
    }

    /**
     * Create order items from cart items
     */
    @Override
    public List<OrderItems> createOrderItems(List<CartItems> cartItems) {
        return cartItems.stream()
                .map(cartItem -> {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(cartItem.getProduct().getPrice());
                    orderItem.setTotalPrice(cartItem.getProductPrice());
                    orderItem.setDiscount(cartItem.getDiscount());
                    orderItem.setTax(cartItem.getTax());
                    orderItem.setImageUrl(cartItem.getProduct().getImageUrl());
                    orderItem.setCreatedAt(now());
                    orderItem.setUpdatedAt(now());
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * Create payment details
     */
    @Override
    public PaymentDetails createPaymentDetails(Checkout checkout, String transactionId) {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentMethod(checkout.getPaymentMethod().name());
        paymentDetails.setTransactionId(transactionId);
        paymentDetails.setPaymentDate(now());

        if (PaymentMethod.CASH_ON_DELIVERY.equals(checkout.getPaymentMethod())) {
            paymentDetails.setPaymentStatus("PENDING");
        } else {
            paymentDetails.setPaymentStatus(
                    PaymentStatus.COMPLETED.equals(checkout.getPaymentStatus())
                            ? "COMPLETED"
                            : "PENDING"
            );
        }

        return paymentDetails;
    }

    /**
     * Get order by payment request ID
     * Validates payment is completed before returning order
     **/
    @Override
    public OrderDTO getOrderByPaymentRequestId(String paymentRequestId) {
        log.info("Fetching order for payment request: {}", paymentRequestId);

        Checkout checkout = checkoutRepository.findByPaymentRequestId(paymentRequestId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found for payment request",
                        HttpStatus.NOT_FOUND.value()
                ));

        if (!PaymentStatus.COMPLETED.equals(checkout.getPaymentStatus())) {
            log.warn("Payment not completed for: {}. Status: {}",
                    paymentRequestId, checkout.getPaymentStatus());
            throw new PaymentException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Payment not yet completed",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Order order = orderRepository.findByTransactionId(paymentRequestId)
                .orElseThrow(() -> new OrderException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Order not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        log.info("Order found: {}", order.getId());

        return mapToOrderDTO(order);
    }
}
