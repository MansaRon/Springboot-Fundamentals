package co.za.ecommerce.business;

import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;

import java.util.List;

public interface CheckoutValidationService {
    void validateCheckout(Checkout checkout);
    void validateInventory(List<CartItems> items);
}
