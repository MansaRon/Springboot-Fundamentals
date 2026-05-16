package co.za.ecommerce.business;

import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.OrderStatus;

public interface EmailService {
    void sendWelcomeEmail(User user);
    void sendOrderConfirmationEmail(User user, OrderDTO order);
    void sendOrderStatusUpdateEmail(User user, OrderDTO order, OrderStatus newStatus);
    void sendPaymentFailureEmail(User user, String reason);
}