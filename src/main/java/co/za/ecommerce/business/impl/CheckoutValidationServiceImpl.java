package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CheckoutValidationService;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutValidationServiceImpl implements CheckoutValidationService {
    private final ProductRepository productRepository;

    @Override
    public void validateCheckout(Checkout checkout) {
        log.info("Validating checkout: {}", checkout.getId());

        validateCheckoutStatus(checkout);
        validatePaymentMethod(checkout);
        validateAddresses(checkout);
        validateShippingMethod(checkout);
        validateInventory(checkout.getItems());
        validatePricing(checkout);

        log.info("Checkout validation successful for: {}", checkout.getId());
    }

    @Override
    public void validateInventory(List<CartItems> items) {
        log.info("Validating inventory for {} items", items.size());

        for (CartItems item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new CheckoutException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Product not found: " + item.getProduct().getTitle(),
                            HttpStatus.NOT_FOUND.value()
                    ));

            if (product.getQuantity() < item.getQuantity()) {
                throw new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        String.format("Insufficient stock for %s. Available: %d, Required: %d",
                                product.getTitle(), product.getQuantity(), item.getQuantity()),
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            if (Math.abs(product.getPrice() - item.getProduct().getPrice()) > 0.01) {
                throw new CheckoutException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Price has changed for: " + product.getTitle() + ". Please review your cart.",
                        HttpStatus.BAD_REQUEST.value()
                );
            }
        }
    }

    private void validateCheckoutStatus(Checkout checkout) {
        if (!CheckoutStatus.PENDING.equals(checkout.getStatus()) &&
                !CheckoutStatus.FAILED.equals(checkout.getStatus())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Cannot process checkout. Current status: " + checkout.getStatus(),
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private void validatePaymentMethod(Checkout checkout) {
        if (PaymentMethod.NOT_SELECTED.equals(checkout.getPaymentMethod())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Payment method is required",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (PaymentMethod.CASH_ON_DELIVERY.equals(checkout.getPaymentMethod())) {
            return;
        }

        if (!isOnlinePaymentMethod(checkout.getPaymentMethod())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid payment method: " + checkout.getPaymentMethod(),
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private void validateAddresses(Checkout checkout) {
        validateAddress(checkout.getShippingAddress(), "Shipping");
        validateAddress(checkout.getBillingAddress(), "Billing");
    }

    private void validateAddress(Address address, String type) {
        if (address == null) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    type + " address is required",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (isNullOrEmpty(address.getStreetAddress())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    type + " street address is required",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (isNullOrEmpty(address.getCity())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    type + " city is required",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (isNullOrEmpty(address.getPostalCode())) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    type + " postal code is required",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private void validateShippingMethod(Checkout checkout) {
        if (checkout.getShippingMethod() == null) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Shipping method is required",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private void validatePricing(Checkout checkout) {
        double calculatedSubtotal = checkout.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        if (Math.abs(calculatedSubtotal - checkout.getSubtotal()) > 0.01) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Subtotal mismatch. Please refresh your cart.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        double expectedTotal = checkout.getSubtotal() - checkout.getDiscount() + checkout.getTax();
        if (Math.abs(expectedTotal - checkout.getTotalAmount()) > 0.01) {
            throw new CheckoutException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Total amount mismatch. Please review your order.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private boolean isOnlinePaymentMethod(PaymentMethod method) {
        return PaymentMethod.CREDIT_CARD.equals(method);
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

}
