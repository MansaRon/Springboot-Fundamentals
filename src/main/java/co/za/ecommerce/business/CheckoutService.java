package co.za.ecommerce.business;

import co.za.ecommerce.dto.PaymentInitializationResponse;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.checkout.CheckoutStatusDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.checkout.Checkout;
import org.bson.types.ObjectId;

import java.util.List;

public interface CheckoutService {
    CheckoutDTO createCheckoutFromCart(ObjectId userId);
    PaymentInitializationResponse initializePayment(ObjectId userId);
    CheckoutDTO getCheckoutByUserId(ObjectId userId);
    CheckoutDTO getCheckoutByCartId(ObjectId cartId);
    List<CheckoutDTO> getCheckoutsByStatus(String status);
    CheckoutDTO updateCheckout(ObjectId checkoutId, CheckoutDTO checkoutDTO);
    void cancelCheckout(ObjectId checkoutId);
    CheckoutDTO deleteCheckoutByUserId(ObjectId userId);
    void handlePaymentCancellation(String paymentRequestId);
    Checkout getCheckoutByPaymentRequestId(String paymentRequestId);
    CheckoutStatusDTO getCheckoutStatus(ObjectId checkoutId);
}
