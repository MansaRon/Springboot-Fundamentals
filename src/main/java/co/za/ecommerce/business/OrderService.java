package co.za.ecommerce.business;

import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.model.order.PaymentDetails;
import org.bson.types.ObjectId;

import java.util.List;

public interface OrderService {
    Order createOrderFromCheckout(Checkout checkout, String transactionId);
    List<OrderItems> createOrderItems(List<CartItems> cartItems);
    PaymentDetails createPaymentDetails(Checkout checkout, String transactionId);
    void clearCart(Cart cart);
}
