package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.EmailService;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.exception.EmailException;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.OrderStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@thendo-ecommerce.co.za}")
    private String fromAddress;

    @Async
    @Override
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to {}", user.getEmail());
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        send(user.getEmail(), "Welcome to Thendo Ecommerce — Account Activated", "email/welcome", ctx);
    }

    @Async
    @Override
    public void sendOrderConfirmationEmail(User user, OrderDTO order) {
        log.info("Sending order confirmation email to {} for order {}", user.getEmail(), order.getOrderNumber());
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("order", order);
        send(user.getEmail(), "Order Confirmed — " + order.getOrderNumber(), "email/order-confirmation", ctx);
    }

    @Async
    @Override
    public void sendOrderStatusUpdateEmail(User user, OrderDTO order, OrderStatus newStatus) {
        log.info("Sending status update email to {} — order {} is now {}", user.getEmail(), order.getOrderNumber(), newStatus);
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("order", order);
        ctx.setVariable("newStatus", newStatus.name());
        String subject = buildStatusSubject(order.getOrderNumber(), newStatus);
        send(user.getEmail(), subject, "email/order-status-update", ctx);
    }

    @Async
    @Override
    public void sendPaymentFailureEmail(User user, String reason) {
        log.info("Sending payment failure email to {}", user.getEmail());
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("reason", reason);
        send(user.getEmail(), "Payment Failed — Action Required", "email/payment-failure", ctx);
    }

    // Sent synchronously (no @Async): the caller overwrites the password only once delivery
    // succeeds, so a mail failure must surface rather than be swallowed.
    @Override
    public void sendPasswordResetEmail(User user, String temporaryPassword) {
        log.info("Sending password reset email to {}", user.getEmail());
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("temporaryPassword", temporaryPassword);
        sendOrThrow(user.getEmail(), "Your Password Has Been Reset", "email/password-reset", ctx);
    }

    void send(String to, String subject, String template, Context ctx) {
        try {
            dispatch(to, subject, template, ctx);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }

    /**
     * Like {@link #send} but rethrows on failure — for mails whose delivery the caller
     * must be able to react to (e.g. admin password resets).
     */
    void sendOrThrow(String to, String subject, String template, Context ctx) {
        try {
            dispatch(to, subject, template, ctx);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
            throw new EmailException("Failed to send email: " + subject, e);
        }
    }

    private void dispatch(String to, String subject, String template, Context ctx) throws Exception {
        String html = templateEngine.process(template, ctx);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
        log.info("Email '{}' sent to {}", subject, to);
    }

    private String buildStatusSubject(String orderNumber, OrderStatus status) {
        return switch (status) {
            case SHIPPED -> "Your Order Has Shipped — " + orderNumber;
            case DELIVERED -> "Your Order Has Been Delivered — " + orderNumber;
            case CANCELLED -> "Order Cancelled — " + orderNumber;
            default -> "Order Update — " + orderNumber;
        };
    }
}