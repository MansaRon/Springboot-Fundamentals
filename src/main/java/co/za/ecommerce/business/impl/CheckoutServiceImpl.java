package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CheckoutService;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentDTO;
import co.za.ecommerce.dto.order.PaymentResultsDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.exception.ValidationException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.*;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.CheckoutRepository;
import co.za.ecommerce.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {
    private final CartRepository cartRepository;
    private final CheckoutRepository checkoutRepository;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    @Override
    public CheckoutDTO initiateCheckout(ObjectId userId) {
        // Creates a checkout entry when a user proceeds to checkout.
        // Retrieves the cart of the user.
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Cart not found for user",
                        HttpStatus.BAD_REQUEST.value()));

        // Ensure the cart has items
        if (cart.getCartItems().isEmpty()) {
            throw new CartException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cart is empty. Cannot proceed to checkout.",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Calculates total price, discounts, taxes, and shipping costs.
        double subTotal = cart.getCartItems()
                .stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        double discount = 0.0;
        double tax = subTotal * 0.1;
        double totalAmount = subTotal + tax - discount;

        Address address = Address.builder()
                .streetAddress("")
                .city("")
                .state("")
                .postalCode("")
                .country("")
                .build();

        // Set up checkout entity.
        Checkout checkout = Checkout.builder()
                .createdAt(now())
                .updatedAt(now())
                .user(cart.getUser())
                .cart(cart)
                .items(cart.getCartItems())
                .subtotal(subTotal)
                .discount(discount)
                .tax(tax)
                .totalAmount(totalAmount)
                .paymentMethod(PaymentMethod.NOT_SELECTED)
                .shippingAddress(address)
                .billingAddress(address)
                .shippingMethod(DeliverMethod.DHL)
                .estimatedDeliveryDate(now().plusDays(5))
                .status(CheckoutStatus.PENDING)
                .build();

        // Saves a new Checkout entry.
        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Convert to DTO
        return objectMapper.mapObject().map(savedCheckout, CheckoutDTO.class);
    }

    @Override
    public CheckoutDTO getCheckoutByUserId(ObjectId userId) {
        // Retrieves an ongoing checkout for a user.
        Checkout retrieveCheckout = checkoutRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "User does not have any checked out items.",
                        HttpStatus.BAD_REQUEST.value()));

        // Fetches the latest checkout in progress for the user.
        // Eg) User logs in and wants to resume their checkout
        return objectMapper.mapObject().map(retrieveCheckout, CheckoutDTO.class);
    }

    @Override
    public CheckoutDTO getCheckoutByCartId(ObjectId cartId) {
        // Retrieves checkout details for a specific cart.
        // Useful if you store checkout history linked to cart IDs.
        // Eg) Admin/user wants to track a checkout for a specific cart
        Checkout checkout = checkoutRepository.findByCartId(cartId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No checkout found for this cart.",
                        HttpStatus.NOT_FOUND.value()));

        return objectMapper.mapObject().map(checkout, CheckoutDTO.class);
    }

    @Override
    public List<CheckoutDTO> getCheckoutsByStatus(String status) {
//        Lists all checkouts filtered by status (e.g., PENDING, COMPLETED).
//        Helps admin or order processing systems filter checkouts.
        CheckoutStatus checkoutStatus = Optional.ofNullable(status)
                .map(s -> {
                    try {
                        return CheckoutStatus.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new CheckoutException(
                                HttpStatus.BAD_REQUEST.toString(),
                                "Invalid checkout status: " + s,
                                HttpStatus.BAD_REQUEST.value()
                        );
                    }
                }).orElseThrow(() -> new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Status cannot be null",
                        HttpStatus.BAD_REQUEST.value()
                ));

        return checkoutRepository.findAllByStatus(checkoutStatus)
                .stream()
                .map(checkout -> objectMapper.mapObject().map(checkout, CheckoutDTO.class))
                .toList();
    }

    @Override
    public CheckoutDTO updateCheckout(ObjectId userId, CheckoutDTO checkoutDTO) {
        Checkout checkout = checkoutRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No active checkout found for user.",
                        HttpStatus.NOT_FOUND.value()));

        if (CheckoutStatus.PENDING.equals(checkout.getStatus())) {
            if (checkoutDTO.getPaymentMethod() != null) {
                checkout.setPaymentMethod(checkoutDTO.getPaymentMethod());
            }
            // Update shipping address if provided
            if (checkoutDTO.getShippingAddress() != null) {
                checkout.setShippingAddress(objectMapper.mapObject()
                        .map(checkoutDTO.getShippingAddress(), Address.class)
                );
            }

            // Update billing address if provided
            if (checkoutDTO.getBillingAddress() != null) {
                checkout.setBillingAddress(objectMapper.mapObject()
                        .map(checkoutDTO.getBillingAddress(), Address.class)
                );
            }

            // Update shipping method if provided
            if (checkoutDTO.getShippingMethod() != null) {
                checkout.setShippingMethod(checkoutDTO.getShippingMethod());
            }

            // Update cart items if provided
            if (checkoutDTO.getItems() != null && !checkoutDTO.getItems().isEmpty()) {
                List<CartItems> updatedCartItems = checkoutDTO.getItems().stream()
                        .map(item -> objectMapper.mapObject().map(item, CartItems.class))
                        .collect(Collectors.toList());
                checkout.setItems(updatedCartItems);
            }

            Checkout savedCheckout = checkoutRepository.save(checkout);
            return objectMapper.mapObject().map(savedCheckout, CheckoutDTO.class);
        } else {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Checkout cannot be updated as it is already " + checkout.getStatus().name().toLowerCase() + ".",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public OrderDTO confirmCheckout(ObjectId checkoutId) {
//        Finalizes checkout, creating an order.
//        Converts checkout into an order and processes payment.
//        Marks checkout as COMPLETED or CANCELLED based on payment success.
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found.",
                        HttpStatus.NOT_FOUND.value()
                ));

        // Validate checkout status
        if (!CheckoutStatus.PENDING.equals(checkout.getStatus())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot confirm checkout. Current status: " + checkout.getStatus(),
                    HttpStatus.BAD_REQUEST.value());
        }

        // Validate payment method
        if (PaymentMethod.NOT_SELECTED.equals(checkout.getPaymentMethod())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Payment method is required to confirm checkout.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Validate shipping and billing addresses
        validateAddress(checkout.getShippingAddress());
        validateAddress(checkout.getBillingAddress());

        if (checkout.getShippingMethod() == null) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Shipping method is required to confirm checkout.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        double discount = calculateDiscount(checkout);
        double tax = calculateTax(checkout);
        checkout.setDiscount(discount);
        checkout.setTax(tax);

        double expectedTotal = checkout.getSubtotal() - checkout.getDiscount() + checkout.getTax();
        if (expectedTotal != checkout.getTotalAmount()) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Order total mismatch. Please review your order before confirming checkout.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        try {
            // TODO
            PaymentResultsDTO paymentResultsDTO = processPayment(checkout);

            if (!paymentResultsDTO.isSuccess()) {
                // Mark checkout as failed if payment fails
                checkout.setStatus(CheckoutStatus.FAILED);
                checkout.setUpdatedAt(now());
                checkoutRepository.save(checkout);

                throw new PaymentException(
                        HttpStatus.PAYMENT_REQUIRED.toString(),
                        "Payment failed",
                        HttpStatus.PAYMENT_REQUIRED.value());
            }

            Order order = createOrderFromCheckout(checkout, paymentResultsDTO.getTransactionId());
            Order savedOrder = orderRepository.save(order);

            checkout.setStatus(CheckoutStatus.COMPLETED);
            checkout.setUpdatedAt(now());
            checkoutRepository.save((checkout));

            Cart cart = checkout.getCart();
            cart.getCartItems().clear();
            cart.updateTotal();
            cartRepository.save(cart);

            return objectMapper.mapObject().map(savedOrder, OrderDTO.class);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            checkout.setStatus(CheckoutStatus.FAILED);
            checkout.setUpdatedAt(now());
            checkoutRepository.save(checkout);

            throw new CheckoutException(
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "Error during checkout confirmation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public void cancelCheckout(ObjectId checkoutId) {
        // Cancels a checkout session if the user decides not to proceed.
        // Removes the checkout entry or marks it as CANCELLED.
        // Cancels a checkout session if the user decides not to proceed.
        Checkout checkout = checkoutRepository.findById(checkoutId).orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found.",
                        HttpStatus.NOT_FOUND.value()));

        // Only pending checkouts can be cancelled
        if (CheckoutStatus.PENDING.equals(checkout.getStatus()) || CheckoutStatus.FAILED.equals(checkout.getStatus())) {
            checkout.setStatus(CheckoutStatus.CANCELLED);
            checkout.setUpdatedAt(now());
            checkoutRepository.save(checkout);
        } else {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot cancel checkout with status: " + checkout.getStatus(),
                    HttpStatus.BAD_REQUEST.value());
        }
    }

    @Override
    public void deleteCheckoutByUserId(ObjectId userId) {
        // Deletes all checkouts for a specific user (e.g., account deletion).
        // Cleans up stale checkouts linked to a user.
        // Deletes all pending checkouts for a specific user
        List<Checkout> userCheckouts = checkoutRepository.findByUserId(userId);

        // Only delete PENDING checkouts
        List<Checkout> pendingCheckouts = userCheckouts.stream()
                .filter(checkout -> CheckoutStatus.PENDING.equals(checkout.getStatus()))
                .collect(Collectors.toList());

        if (!pendingCheckouts.isEmpty()) {
            checkoutRepository.deleteAll(pendingCheckouts);
        }
    }

    private void validateAddress(Address address) {
        if (address == null ||
                StringUtils.hasText(address.getStreetAddress()) ||
                StringUtils.hasText(address.getCity()) ||
                StringUtils.hasText(address.getState()) ||
                StringUtils.hasText(address.getPostalCode()) ||
                StringUtils.hasText(address.getCountry())) {

            throw new ValidationException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Complete address information is required.",
                    HttpStatus.BAD_REQUEST.value());
        }
    }

    private Order createOrderFromCheckout(Checkout checkout, String transactionId) {
        // Create a new order from checkout details
        Order order = new Order();

        // Set basic order information
        order.setCustomerInfo(checkout.getUser());
        order.setCreatedAt(now());
        order.setUpdatedAt(now());

        // Convert cart items to order items
        List<OrderItems> orderItems = checkout.getItems().stream()
                .map(this::cartItemToOrderItem)
                .collect(Collectors.toList());
        order.setOrderItems(orderItems);

        // Set financial details
        order.setSubtotal(checkout.getSubtotal());
        order.setDiscount(checkout.getDiscount());
        order.setTax(checkout.getTax());
        order.setShippingCost(calculateShippingCost(checkout.getShippingMethod()));
        order.setTotalAmount(checkout.getTotalAmount());

        // Set addresses
        order.setShippingAddress(checkout.getShippingAddress());
        order.setBillingAddress(checkout.getBillingAddress());

        // Set delivery information
        order.setShippingMethod(checkout.getShippingMethod().toString());
        order.setEstimatedDeliveryDate(checkout.getEstimatedDeliveryDate());

        // Set payment information
        order.setPaymentMethod(checkout.getPaymentMethod());
        order.setTransactionId(transactionId);

        // Set initial order status
        order.setOrderStatus(OrderStatus.PROCESSING);

        return order;
    }

    private OrderItems cartItemToOrderItem(CartItems cartItem) {
        OrderItems orderItem = new OrderItems();
        // orderItem.setProduct(cartItem.getProduct());
        orderItem.setQuantity(cartItem.getQuantity());
        // orderItem.setUnitPrice(cartItem.getProduct().getPrice());
        orderItem.setTotalPrice(cartItem.getProductPrice());
        // orderItem.setDiscount(cartItem.getDiscount());
        // orderItem.setTax(cartItem.getTax());
        return orderItem;
    }

    private PaymentResultsDTO processPayment(Checkout checkout) {
        // Create payment details from checkout information
        PaymentDTO paymentDetails = new PaymentDTO();
        paymentDetails.setPaymentMethod(checkout.getPaymentMethod());

        // Additional payment details would be populated from
        // the request in a real implementation

        // Process payment using payment processor
        return paymentProcessor.processPayment(
                checkout.getTotalAmount(),
                paymentDetails,
                checkout.getUser().getId().toString());
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

    private double calculateTax(Checkout checkout) {
        double taxRate = 0.08; // Example: 8% tax rate
        return (checkout.getSubtotal() - checkout.getDiscount()) * taxRate;
    }

    private double calculateDiscount(Checkout checkout) {
        double discount = 0.0;

        // Example: Apply a 10% discount if total is over $100
        if (checkout.getSubtotal() > 100) {
            discount = checkout.getSubtotal() * 0.10;
        }

        // TODO: Apply coupon-based or user-specific discounts
        return discount;
    }

}
