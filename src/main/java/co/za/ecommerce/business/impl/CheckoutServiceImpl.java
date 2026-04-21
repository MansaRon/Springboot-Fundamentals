package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.mapper.CheckoutMapper;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.*;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.CheckoutRepository;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.utils.DateUtil;
import co.za.ecommerce.utils.ValueUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {
    private final CartRepository cartRepository;
    private final CheckoutRepository checkoutRepository;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final CheckoutValidationService validationService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final CartService cartService;

    @Override
    @Transactional
    public CheckoutDTO createCheckoutFromCart(ObjectId userId) {
        log.info("Creating checkout from cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No active cart found for user",
                        HttpStatus.NOT_FOUND.value()
                ));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot checkout with empty cart",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Optional<Checkout> existingCheckout = checkoutRepository.findByCartId(cart.getId());
        if (existingCheckout.isPresent() &&
                CheckoutStatus.PENDING.equals(existingCheckout.get().getStatus()) ||
        CheckoutStatus.FAILED.equals(existingCheckout.get().getStatus())) {
            log.info("Returning existing checkout for cart: {}", cart.getId());
            return CheckoutMapper.toDTO(existingCheckout.get());
        }

        Checkout checkout = new Checkout();
        checkout.setCreatedAt(LocalDateTime.now());
        checkout.setUpdatedAt(LocalDateTime.now());
        checkout.setUser(cart.getUser());
        checkout.setCart(cart);
        checkout.setItems(new ArrayList<>(cart.getCartItems()));
        checkout.setStatus(CheckoutStatus.PENDING);
        checkout.setPaymentMethod(PaymentMethod.NOT_SELECTED);
        checkout.setCurrency("ZAR");
        checkout.setBillingAddress(null);
        checkout.setShippingAddress(null);
        checkout.setEstimatedDeliveryDate(DateUtil.getEstimatedDeliveryDate().atStartOfDay());
        checkout.setShippingMethod(DeliverMethod.FREE);

        recalculateTotals(checkout);

        Checkout savedCheckout = checkoutRepository.save(checkout);

        log.info("====================Checkout ID==========================");
        log.info("Checkout created successfully: {}", savedCheckout.getId());

        return CheckoutMapper.toDTO(savedCheckout);
    }

    @Override
    public CheckoutDTO getCheckoutByUserId(ObjectId userId) {
        Checkout retrieveCheckout = checkoutRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "User does not have any checked out items.",
                        HttpStatus.BAD_REQUEST.value()));
        return CheckoutMapper.toDTO(retrieveCheckout);
    }

    @Override
    public CheckoutDTO getCheckoutByCartId(ObjectId cartId) {
        Checkout checkout = checkoutRepository.findByCartId(cartId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No checkout found for this cart.",
                        HttpStatus.NOT_FOUND.value())
                );
        return CheckoutMapper.toDTO(checkout);
    }

    @Override
    public List<CheckoutDTO> getCheckoutsByStatus(String status) {
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
                .map(CheckoutMapper::toDTO)
                .toList();
    }

    @Override
    public CheckoutDTO updateCheckout(ObjectId userId, CheckoutDTO checkoutDTO) {
        Checkout checkout = checkoutRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "User does not have any checked out items.",
                        HttpStatus.BAD_REQUEST.value()));

        if (!CheckoutStatus.PENDING.equals(checkout.getStatus()) &&
        !CheckoutStatus.FAILED.equals(checkout.getStatus())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Checkout cannot be updated as it is already " + checkout.getStatus().name().toLowerCase() + ".",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        validateCheckoutReferences(checkout);

        updatePaymentMethod(checkout, checkoutDTO);
        updateShippingAddress(checkout, checkoutDTO);
        updateBillingAddress(checkout, checkoutDTO);
        updateShippingMethod(checkout, checkoutDTO);
        updateCartItems(checkout, checkoutDTO);
        recalculateTotals(checkout);
        log.info("Received checkoutDTO: paymentMethod={}, shippingAddress={}, billingAddress={}, shippingMethod={}",
                checkoutDTO.getPaymentMethod(),
                checkoutDTO.getShippingAddress(),
                checkoutDTO.getBillingAddress(),
                checkoutDTO.getShippingMethod());
        Checkout savedCheckout = checkoutRepository.save(checkout);
        return CheckoutMapper.toDTO(savedCheckout);
    }

    @Override
    public void cancelCheckout(ObjectId checkoutId) {
        Checkout checkout = checkoutRepository.findById(checkoutId).orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found.",
                        HttpStatus.NOT_FOUND.value()));

        if (!CheckoutStatus.PENDING.equals(checkout.getStatus()) &&
                !CheckoutStatus.FAILED.equals(checkout.getStatus())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot cancel checkout with status: " + checkout.getStatus(),
                    HttpStatus.BAD_REQUEST.value());
        }

        checkout.setStatus(CheckoutStatus.CANCELLED);
        checkout.setUpdatedAt(now());
        checkoutRepository.save(checkout);
    }

    @Override
    public CheckoutDTO deleteCheckoutByUserId(ObjectId userId) {
        List<Checkout> pendingCheckouts = checkoutRepository.findByUserId(userId).stream()
                .filter(checkout -> CheckoutStatus.PENDING.equals(checkout.getStatus()))
                .collect(Collectors.toList());

        if (pendingCheckouts.isEmpty()) {
            throw new CheckoutException(
                    HttpStatus.NOT_FOUND.toString(),
                    "No pending checkouts found for user.",
                    HttpStatus.NOT_FOUND.value()
            );
        }

        checkoutRepository.deleteAll(pendingCheckouts);

        return objectMapper.mapObject().map(
                    pendingCheckouts.get(pendingCheckouts.size() - 1),
                    CheckoutDTO.class
            );
    }

    @Override
    @Transactional
    public OrderDTO confirmCheckout(ObjectId checkoutId) {
        log.info("========================== Confirming Checkout ================================");
        log.info("Checkout ID: {}", checkoutId);

        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        if (!CheckoutStatus.PENDING.equals(checkout.getStatus()) &&
        !CheckoutStatus.FAILED.equals(checkout.getStatus())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot confirm checkout. Current status: " + checkout.getStatus(),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        recalculateTotals(checkout);
        checkoutRepository.save(checkout);

        validationService.validateCheckout(checkout);

        try {
            log.info("Processing payment for checkout: {}", checkout);
            PaymentResultDTO paymentResult = paymentService.processPayment(checkout);

            if (!paymentResult.isSuccess()) {
                handlePaymentFailure(checkout, paymentResult);
                throw new PaymentException(
                        "PAYMENT_FAILED",
                        paymentResult.getFailureReason(),
                        HttpStatus.PAYMENT_REQUIRED.value()
                );
            }

            log.info("Payment successful! Creating order for checkout: {}", checkoutId);
            OrderDTO order = orderService.createOrderFromCheckout(checkout, paymentResult);

            checkout.setStatus(CheckoutStatus.COMPLETED);
            checkout.setUpdatedAt(now());
            checkoutRepository.save(checkout);

            cartService.deleteCart(checkout.getCart());

            log.info("✅ Order created successfully: {}", order.getTransactionId());
            return order;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            checkout.setStatus(CheckoutStatus.FAILED);
            checkout.setUpdatedAt(now());
            checkoutRepository.save(checkout);

            log.error("Error during checkout confirmation: {}", e.getMessage(), e);
            throw new CheckoutException(
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "Error during checkout confirmation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    private Checkout findActiveCheckoutByUserId(ObjectId userId) {
        return checkoutRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No active checkout found for user.",
                        HttpStatus.NOT_FOUND.value()));
    }

    private void handlePaymentFailure(Checkout checkout, PaymentResultDTO paymentResultDTO) {
        log.warn("Payment failed for checkout: {}", checkout.getId());
        log.warn("Reason: {}", paymentResultDTO.getFailureReason());

        checkout.setStatus(CheckoutStatus.FAILED);
        checkout.setUpdatedAt(now());
        checkoutRepository.save(checkout);
    }

    private void validateCheckoutReferences(Checkout checkout) {
        if (checkout.getUser() == null || checkout.getUser().getId() == null) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Checkout user reference is missing.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
        if (checkout.getCart() == null || checkout.getCart().getId() == null) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Checkout cart reference is missing.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
        if (checkout.getItems() != null) {
            checkout.getItems().forEach(item -> {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    throw new CheckoutException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Cart item is missing a product reference.",
                            HttpStatus.BAD_REQUEST.value()
                    );
                }
            });
        }
    }

    private void updatePaymentMethod(Checkout checkout, CheckoutDTO dto) {
        if (dto.getPaymentMethod() != null) {
            checkout.setPaymentMethod(dto.getPaymentMethod());
        }
    }

    private void updateShippingAddress(Checkout checkout, CheckoutDTO dto) {
        log.debug("Shipping address checkout: {}", dto.getShippingAddress());
        if (dto.getShippingAddress() != null) {
            checkout.setShippingAddress(
                    CheckoutMapper.toAddress(dto.getShippingAddress())
            );
        }
    }

    private void updateBillingAddress(Checkout checkout, CheckoutDTO dto) {
        log.debug("Billing address checkout: {}", dto.getBillingAddress());
        if (dto.getBillingAddress() != null) {
            checkout.setBillingAddress(
                    CheckoutMapper.toAddress(dto.getShippingAddress())
            );
        }
    }

    private void updateShippingMethod(Checkout checkout, CheckoutDTO dto) {
        if (dto.getShippingMethod() != null) {
            checkout.setShippingMethod(dto.getShippingMethod());
        }
    }

    private void updateCartItems(Checkout checkout, CheckoutDTO checkoutDTO) {
        if (checkoutDTO.getItems() == null || checkoutDTO.getItems().isEmpty()) {
            return;
        }

        List<CartItems> updatedCartItems = checkoutDTO.getItems().stream()
                .map(dto -> {
                    CartItems entity = new CartItems();

                    if (dto.getProductDTO() == null) {
                        throw new CheckoutException(
                                HttpStatus.BAD_REQUEST.toString(),
                                "Cart item is missing a product.",
                                HttpStatus.BAD_REQUEST.value()
                        );
                    }

                    Product product = productRepository.findById(
                            new ObjectId(dto.getProductDTO().getId()))
                                .orElseThrow(() -> new CheckoutException(
                                        HttpStatus.NOT_FOUND.toString(),
                                        "Product not found with ID: " + dto.getProductDTO().getId(),
                                        HttpStatus.NOT_FOUND.value()
                                ));

                    return CartItems.builder()
                            .product(product)
                            .quantity(dto.getQuantity())
                            .discount(dto.getDiscount())
                            .tax(dto.getTax())
                            .productPrice(dto.getProductPrice())
                            .build();
                }).collect(Collectors.toList());

        checkout.setItems(updatedCartItems);
    }

    private void recalculateTotals(Checkout checkout) {
        log.debug("Recalculating totals for checkout: {}", checkout.getId());
        double subTotal = checkout
                .getItems()
                .stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        double discount = checkout.getDiscount();
        double tax = subTotal * 0.10;
        double totalAmount = (subTotal - discount) + tax;

        checkout.setSubtotal(subTotal);
        checkout.setDiscount(discount);
        // TODO remove util method and utilise BigDecimal for precise numbers
        checkout.setTax(ValueUtil.round(tax, 2));
        checkout.setTotalAmount(ValueUtil.round(totalAmount, 2));

        log.debug("Totals: Subtotal=R{}, Discount=R{}, Tax=R{}, Total=R{}",
                subTotal, discount, tax, totalAmount);
    }

}
