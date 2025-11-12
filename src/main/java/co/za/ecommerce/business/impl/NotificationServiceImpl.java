package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.NotificationService;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.model.order.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final JavaMailSender mailSender;

    /**
     * Send order confirmation email
     */
    @Async
    @Override
    public void sendOrderConfirmationEmail(Order order) {
        try {
            log.info("Sending order confirmation email for order: {}", order.getId());

            String to = order.getCustomerInfo().getEmail();
            String subject = "Order Confirmation - Order #" + order.getId();
            String body = buildOrderConfirmationEmailBody(order);

            sendEmail(to, subject, body);

            log.info("Order confirmation email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment failure notification
     */
    @Async
    @Override
    public void sendPaymentFailureEmail(Checkout checkout) {
        try {
            log.info("Sending payment failure email for checkout: {}", checkout.getId());

            String to = checkout.getUser().getEmail();
            String subject = "Payment Failed - Please Try Again";
            String body = buildPaymentFailureEmailBody(checkout);

            sendEmail(to, subject, body);

            log.info("Payment failure email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment failure email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send order confirmation SMS (stub)
     */
    @Async
    @Override
    public void sendOrderConfirmationSMS(String phoneNumber, Order order) {
        try {
            log.info("Sending SMS to {}: Order #{} confirmed", phoneNumber, order.getId());

            // TODO: Integrate with SMS provider (Twilio, Africa's Talking, etc.)
            String message = String.format(
                    "Your order #%s has been confirmed. Total: R%.2f. Track your order at: https://yourdomain.com/orders/%s",
                    order.getId(), order.getTotalAmount(), order.getId()
            );

            // sendSMS(phoneNumber, message);

            log.info("SMS sent successfully (stub)");
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage(), e);
        }
    }

    /**
     * Build order confirmation email body
     */
    private String buildOrderConfirmationEmailBody(Order order) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(order.getCustomerInfo().getName()).append(",\n\n");
        body.append("Thank you for your order!\n\n");
        body.append("Order Details:\n");
        body.append("Order Number: ").append(order.getId()).append("\n");
        body.append("Order Date: ").append(order.getCreatedAt()).append("\n");
        body.append("Total Amount: R").append(String.format("%.2f", order.getTotalAmount())).append("\n\n");

        body.append("Items:\n");
        order.getOrderItems().forEach(item -> {
            body.append("- ").append(item.getProduct().getTitle())
                    .append(" x ").append(item.getQuantity())
                    .append(" - R").append(String.format("%.2f", item.getTotalPrice()))
                    .append("\n");
        });

        body.append("\nShipping Address:\n");
        Address shipping = order.getShippingAddress();
        body.append(shipping.getStreetAddress()).append("\n");
        body.append(shipping.getCity()).append(", ").append(shipping.getState()).append("\n");
        body.append(shipping.getPostalCode()).append("\n");
        body.append(shipping.getCountry()).append("\n\n");

        body.append("Estimated Delivery: ").append(order.getEstimatedDeliveryDate()).append("\n\n");
        body.append("Track your order: https://yourdomain.com/orders/").append(order.getId()).append("\n\n");
        body.append("Thank you for shopping with us!\n");

        return body.toString();
    }

    /**
     * Build payment failure email body
     */
    private String buildPaymentFailureEmailBody(Checkout checkout) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(checkout.getUser().getName()).append(",\n\n");
        body.append("We were unable to process your payment.\n\n");
        body.append("Checkout ID: ").append(checkout.getId()).append("\n");
        body.append("Amount: R").append(String.format("%.2f", checkout.getTotalAmount())).append("\n\n");

        if (checkout.getLastPaymentError() != null) {
            body.append("Reason: ").append(checkout.getLastPaymentError()).append("\n\n");
        }

        body.append("Please try again or contact our support team if you continue to experience issues.\n\n");
        body.append("Retry payment: https://yourdomain.com/checkout/").append(checkout.getId()).append("\n\n");
        body.append("Best regards,\nYour Store Team\n");

        return body.toString();
    }

    /**
     * Send email helper method
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            // If you have JavaMailSender configured
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@yourdomain.com");

            // Uncomment when email is configured
            mailSender.send(message);

            // For now, just log
            log.info("Email would be sent to: {}", to);
            log.info("Subject: {}", subject);
            log.debug("Body: {}", body);

        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
            throw e;
        }
    }

}
