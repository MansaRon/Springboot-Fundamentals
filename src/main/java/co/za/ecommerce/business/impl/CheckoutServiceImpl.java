package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CheckoutService;
import co.za.ecommerce.business.CheckoutValidationService;
import co.za.ecommerce.business.PayFastService;
import co.za.ecommerce.business.PaymentService;
import co.za.ecommerce.dto.PaymentInitializationResponse;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.checkout.CheckoutStatusDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentDTO;
import co.za.ecommerce.dto.order.PaymentResultsDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.exception.ValidationException;
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
import co.za.ecommerce.repository.OrderRepository;
import co.za.ecommerce.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
    private final ProductRepository productRepository;
    private final CheckoutValidationService validationService;
    private final PayFastService payFastService;

    @Override
    public CheckoutDTO createCheckoutFromCart(ObjectId userId) {
        log.info("Creating checkout from cart for user: {}", userId);

        // 1. Find user's active cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No active cart found for user",
                        HttpStatus.NOT_FOUND.value()
                ));

        // 2. Check if cart is empty
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot checkout with empty cart",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // 3. Check if checkout already exists for this cart
        Optional<Checkout> existingCheckout = checkoutRepository.findByCartId(cart.getId());
        if (existingCheckout.isPresent() &&
                CheckoutStatus.PENDING.equals(existingCheckout.get().getStatus())) {
            // Return existing pending checkout
            log.info("Returning existing checkout for cart: {}", cart.getId());
            return CheckoutMapper.toDTO(existingCheckout.get());
        }

        // 4. Create new checkout
        Checkout checkout = new Checkout();
        checkout.setCreatedAt(LocalDateTime.now());
        checkout.setUpdatedAt(LocalDateTime.now());
        checkout.setUser(cart.getUser());
        checkout.setCart(cart);

        // Copy cart items to checkout
        List<CartItems> checkoutItems = new ArrayList<>(cart.getCartItems());
        checkout.setItems(checkoutItems);

        // Calculate totals
        double subtotal = checkoutItems.stream()
                .mapToDouble(CartItems::getProductPrice)
                .sum();
        checkout.setSubtotal(subtotal);

        double discount = calculateDiscount(checkout);
        checkout.setDiscount(discount);

        double tax = calculateTax(checkout);
        checkout.setTax(tax);

        double total = subtotal - discount + tax;
        checkout.setTotalAmount(total);

        // Set initial status
        checkout.setStatus(CheckoutStatus.PENDING);
        checkout.setPaymentStatus(PaymentStatus.PENDING);
        checkout.setPaymentMethod(PaymentMethod.NOT_SELECTED);
        checkout.setCurrency("ZAR");

        // Save checkout
        Checkout savedCheckout = checkoutRepository.save(checkout);

        log.info("Checkout created successfully: {}", savedCheckout.getId());

        return CheckoutMapper.toDTO(savedCheckout);
    }

    @Override
    public PaymentInitializationResponse initializePayment(ObjectId checkoutId) {
        // Creates a checkout entry when a user proceeds to checkout.
        // Retrieves the cart of the user.
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        validationService.validateCheckout(checkout);

        if (PaymentStatus.INITIATED.equals(checkout.getPaymentStatus()) && checkout.getPaymentInitiatedAt() != null) {
            // Allow retry if more than 30 minutes have passed
            if (checkout.getPaymentInitiatedAt().plusMinutes(30).isAfter(now())) {
                log.warn("Payment already initiated for checkout: {}", checkoutId);
                throw new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Payment already in progress. Please complete or wait 30 minutes to retry.",
                        HttpStatus.BAD_REQUEST.value()
                );
            }
        }

        PaymentInitializationResponse response = payFastService.initializePayment(checkout);

        checkout.setPaymentRequestId(response.getPaymentRequestId());
        checkout.setPaymentSignature(response.getSignature());
        checkout.setPaymentStatus(PaymentStatus.INITIATED);
        checkout.setPaymentInitiatedAt(LocalDateTime.now());
        checkout.setPaymentAttempts(checkout.getPaymentAttempts() + 1);
        checkout.setStatus(CheckoutStatus.PENDING);
        checkout.setUpdatedAt(LocalDateTime.now());

        checkoutRepository.save(checkout);

        log.info("Payment initialized successfully for checkout: {}", checkoutId);
        return response;
    }

    @Override
    public void handlePaymentCancellation(String paymentRequestId) {
        log.info("Handling payment cancellation for: {}", paymentRequestId);

        Checkout checkout = checkoutRepository.findByPaymentRequestId(paymentRequestId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found for payment request",
                        HttpStatus.NOT_FOUND.value()
                ));

        checkout.setPaymentStatus(PaymentStatus.DECLINED);
        checkout.setStatus(CheckoutStatus.CANCELLED);
        checkout.setLastPaymentError("Payment cancelled by user");
        checkout.setUpdatedAt(now());

        checkoutRepository.save(checkout);
    }

    @Override
    public Checkout getCheckoutByPaymentRequestId(String paymentRequestId) {
        return checkoutRepository.findByPaymentRequestId(paymentRequestId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found for payment request",
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    @Override
    public CheckoutStatusDTO getCheckoutStatus(ObjectId checkoutId) {
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Checkout not found",
                        HttpStatus.NOT_FOUND.value()
                ));

        return CheckoutStatusDTO.builder()
                .checkoutId(checkout.getId().toString())
                .status(checkout.getStatus())
                .paymentStatus(checkout.getPaymentStatus())
                .totalAmount(checkout.getTotalAmount())
                .paymentMethod(checkout.getPaymentMethod())
                .build();
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
        return CheckoutMapper.toDTO(retrieveCheckout);
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

        return CheckoutMapper.toDTO(checkout);
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
                .map(CheckoutMapper::toDTO)
                .toList();
    }

    @Override
    public CheckoutDTO updateCheckout(ObjectId userId, CheckoutDTO checkoutDTO) {
        Checkout checkout = findActiveCheckoutByUserId(userId);

        if (PaymentStatus.INITIATED.equals(checkout.getPaymentStatus())
                || PaymentStatus.COMPLETED.equals(checkout.getPaymentStatus())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot update checkout - payment already initiated",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (!CheckoutStatus.PENDING.equals(checkout.getStatus())) {
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

        Checkout savedCheckout = checkoutRepository.save(checkout);
        return CheckoutMapper.toDTO(savedCheckout);
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
    public CheckoutDTO deleteCheckoutByUserId(ObjectId userId) {
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

        // Return the last deleted checkout or null if none were deleted
        return pendingCheckouts.isEmpty() ? null :
            objectMapper.mapObject().map(pendingCheckouts.get(pendingCheckouts.size() - 1), CheckoutDTO.class);
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

    private Checkout findActiveCheckoutByUserId(ObjectId userId) {
        return checkoutRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new CheckoutException(
                        HttpStatus.NOT_FOUND.toString(),
                        "No active checkout found for user.",
                        HttpStatus.NOT_FOUND.value()));
    }

    private void validateCheckoutReferences(Checkout checkout) {
        if (checkout.getUser() == null || checkout.getUser().getId() == null) {
            throw new IllegalStateException("Checkout user reference is null or has no ID");
        }
        if (checkout.getCart() == null || checkout.getCart().getId() == null) {
            throw new IllegalStateException("Checkout cart reference is null or has no ID");
        }
        if (checkout.getItems() != null) {
            checkout.getItems().forEach(item -> {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    throw new IllegalStateException("CartItem product reference is null or has no ID");
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
        if (dto.getShippingAddress() != null) {
            checkout.setShippingAddress(
                    objectMapper.mapObject().map(dto.getShippingAddress(), Address.class)
            );
        }
    }

    private void updateBillingAddress(Checkout checkout, CheckoutDTO dto) {
        if (dto.getBillingAddress() != null) {
            checkout.setBillingAddress(
                    objectMapper.mapObject().map(dto.getBillingAddress(), Address.class)
            );
        }
    }

    private void updateShippingMethod(Checkout checkout, CheckoutDTO dto) {
        if (dto.getShippingMethod() != null) {
            checkout.setShippingMethod(dto.getShippingMethod());
        }
    }

    private void updateCartItems(Checkout checkout, CheckoutDTO checkoutDTO) {
        if (checkoutDTO.getItems() != null && !checkoutDTO.getItems().isEmpty()) {
            List<CartItems> updatedCartItems = checkoutDTO.getItems().stream()
                    .map(dto -> {
                        CartItems entity = new CartItems();

                        if (dto.getProductDTO() == null) {
                            throw new IllegalArgumentException("ProductDTO cannot be null");
                        }

                        Product product = null;
                        if (dto.getProductDTO().getId() != null) {
                            product = productRepository.findById(new ObjectId(dto.getProductDTO().getId()))
                                    .orElseThrow(() -> new IllegalArgumentException(
                                            "Product not found with ID: " + dto.getProductDTO().getId()
                                    ));
                        }
                        entity.setProduct(product);
                        entity.setQuantity(dto.getQuantity());
                        entity.setDiscount(dto.getDiscount());
                        entity.setTax(dto.getTax());
                        entity.setProductPrice(dto.getProductPrice());

                        return entity;
                    }).collect(Collectors.toList());

            checkout.setItems(updatedCartItems);
        }
    }

    private void recalculateTotals(Checkout checkout) {
        double subTotal = checkout.getItems().stream().mapToDouble(CartItems::getProductPrice).sum();
        double discount = checkout.getDiscount();
        double tax = subTotal * 0.10;
        double totalAmount = (subTotal - discount) + tax;

        checkout.setSubtotal(subTotal);
        checkout.setDiscount(discount);
        checkout.setTax(tax);
        checkout.setTotalAmount(totalAmount);
    }
}
