package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.model.order.OrderStatus;
import co.za.ecommerce.model.order.PaymentDetails;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;
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
        clearCart(checkout.getCart());

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
     * Clear cart after successful order creation
     */
    @Override
    public void clearCart(Cart cart) {
        log.info("Clearing cart: {}", cart.getId());
        cart.getCartItems().clear();
        cart.updateTotal();
        cartRepository.save(cart);
    }

    private OrderItems cartItemToOrderItem(CartItems cartItem) {
        return OrderItems.builder()
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getProduct().getPrice())
                .totalPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity())
                .discount(0.0) // Can be enhanced with product-specific discounts
                .tax(cartItem.getProduct().getPrice() * cartItem.getQuantity() * 0.08) // Using standard tax rate
                .build();
    }

    private double calculateShippingCost(DeliverMethod deliverMethod) {
        // Calculate shipping cost based on delivery method
        return switch (deliverMethod) {
            case DHL -> 15.99;
            case FedEx -> 12.99;
            case Express -> 10.99;
            case FREE -> 0.0;
        };
    }

}
