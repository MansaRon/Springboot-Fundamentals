package co.za.ecommerce.business;

import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.Order;

public interface NotificationService {
    void sendOrderConfirmationEmail(Order order);
    void sendPaymentFailureEmail(Checkout checkout);
    void sendOrderConfirmationSMS(String phoneNumber, Order order);
}
