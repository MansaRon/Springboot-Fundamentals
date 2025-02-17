package co.za.ecommerce.business;

import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import org.bson.types.ObjectId;

import java.util.List;

public interface CheckoutService {
    CheckoutDTO initiateCheckout(ObjectId userId);
    CheckoutDTO getCheckoutByUserId(ObjectId userId);
    CheckoutDTO getCheckoutByCartId(ObjectId cartId);
    List<CheckoutDTO> getCheckoutsByStatus(String status);
    CheckoutDTO updateCheckout(ObjectId checkoutId, CheckoutDTO checkoutDTO);
    OrderDTO confirmCheckout(ObjectId checkoutId);
    void cancelCheckout(ObjectId checkoutId);
    void deleteCheckoutByUserId(ObjectId userId);
}
