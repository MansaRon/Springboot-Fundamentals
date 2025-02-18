package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CheckoutService;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.repository.CartRepository;
import co.za.ecommerce.repository.CheckoutRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {
    private final CartRepository cartRepository;
    private final CheckoutRepository checkoutRepository;
    private final ObjectMapper objectMapper;

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
        return objectMapper.mapObject().map(retrieveCheckout, CheckoutDTO.class);
    }

    @Override
    public CheckoutDTO getCheckoutByCartId(ObjectId cartId) {
        // Retrieves checkout details for a specific cart.
        // Useful if you store checkout history linked to cart IDs.
        return null;
    }

    @Override
    public List<CheckoutDTO> getCheckoutsByStatus(String status) {
//        Lists all checkouts filtered by status (e.g., PENDING, COMPLETED).
//        Helps admin or order processing systems filter checkouts.
        return List.of();
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
        return null;
    }

    @Override
    public void cancelCheckout(ObjectId checkoutId) {
//        Cancels a checkout session if the user decides not to proceed.
//        Removes the checkout entry or marks it as CANCELLED.
    }

    @Override
    public void deleteCheckoutByUserId(ObjectId userId) {
//        Deletes all checkouts for a specific user (e.g., account deletion).
//        Cleans up stale checkouts linked to a user.
    }
}
