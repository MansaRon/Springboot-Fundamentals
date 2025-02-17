package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CartService;
import co.za.ecommerce.business.CheckoutService;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    @Override
    public CheckoutDTO initiateCheckout(ObjectId userId) {
        // Creates a checkout entry when a user proceeds to checkout.
//        Retrieves the cart of the user.
//        Calculates total price, discounts, taxes, and shipping costs.
//        Saves a new Checkout entry.
        return null;
    }

    @Override
    public CheckoutDTO getCheckoutByUserId(ObjectId userId) {
//        Retrieves an ongoing checkout for a user.
//        Fetches the latest checkout in progress for the user.
        return null;
    }

    @Override
    public CheckoutDTO getCheckoutByCartId(ObjectId cartId) {
//        Retrieves checkout details for a specific cart.
//        Useful if you store checkout history linked to cart IDs.
        return null;
    }

    @Override
    public List<CheckoutDTO> getCheckoutsByStatus(String status) {
//        Lists all checkouts filtered by status (e.g., PENDING, COMPLETED).
//        Helps admin or order processing systems filter checkouts.
        return List.of();
    }

    @Override
    public CheckoutDTO updateCheckout(ObjectId checkoutId, CheckoutDTO checkoutDTO) {
//        Updates checkout details (e.g., payment method, shipping address).
//        Allows users to change their payment method, shipping info, etc. before confirming.
        return null;
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
