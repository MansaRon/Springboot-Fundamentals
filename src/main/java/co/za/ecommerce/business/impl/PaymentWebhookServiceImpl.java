package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.NotificationService;
import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.business.PayFastService;
import co.za.ecommerce.business.PaymentWebhookService;
import co.za.ecommerce.dto.PayFastITNPayload;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.model.PayfastConfig;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.repository.CheckoutRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {
    private final PayFastService payFastService;
    private final PayfastConfig payFastConfig;
    private final CheckoutRepository checkoutRepository;
    private final OrderService orderCreationService;
    private final NotificationService notificationService;

    /**
     * Process PayFast ITN (Instant Transaction Notification)
     * This is called by PayFast webhook
     */
    @Override
    public void processPayFastITN(PayFastITNPayload payload, HttpServletRequest request) {
        log.info("=== Processing PayFast ITN ===");
        log.info("Payment ID: {}", payload.getM_payment_id());
        log.info("PayFast Payment ID: {}", payload.getPf_payment_id());
        log.info("Payment Status: {}", payload.getPayment_status());
        log.info("Amount: {}", payload.getAmount_gross());

        try {
            // 1. Verify IP address (security check)
            if (!verifyPayFastIP(request)) {
                log.error("ITN received from invalid IP: {}", request.getRemoteAddr());
                throw new PaymentException(
                        HttpStatus.FORBIDDEN.toString(),
                        "Invalid source IP",
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // 2. Verify signature
            if (!payFastService.verifySignature(payload)) {
                log.error("Invalid signature for payment: {}", payload.getM_payment_id());
                throw new PaymentException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Invalid signature",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // 3. Get checkout
            Checkout checkout = checkoutRepository.findByPaymentRequestId(payload.getM_payment_id())
                    .orElseThrow(() -> new PaymentException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Checkout not found for payment request",
                            HttpStatus.NOT_FOUND.value()
                    ));

            // 4. Check for duplicate processing (idempotency)
            if (PaymentStatus.COMPLETED.equals(checkout.getPaymentStatus())) {
                log.warn("Payment already processed for checkout: {}", checkout.getId());
                return; // Already processed, return success
            }

            // 5. Verify payment amount
            double expectedAmount = checkout.getTotalAmount();
            double receivedAmount = Double.parseDouble(payload.getAmount_gross());

            if (Math.abs(expectedAmount - receivedAmount) > 0.01) {
                log.error("Amount mismatch! Expected: {}, Received: {}", expectedAmount, receivedAmount);
                handlePaymentFailure(checkout, "Amount mismatch");
                throw new PaymentException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Payment amount mismatch",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // 6. Verify with PayFast API (additional security)
            if (!payFastService.verifyPaymentWithPayFast(payload)) {
                log.error("PayFast API verification failed for: {}", payload.getM_payment_id());
                handlePaymentFailure(checkout, "PayFast verification failed");
                throw new PaymentException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Payment verification failed",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // 7. Process based on payment status
            if ("COMPLETE".equalsIgnoreCase(payload.getPayment_status())) {
                handlePaymentSuccess(checkout, payload);
            } else {
                handlePaymentFailure(checkout, "Payment status: " + payload.getPayment_status());
            }

        } catch (Exception e) {
            log.error("Error processing ITN: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle successful payment
     */
    private void handlePaymentSuccess(Checkout checkout, PayFastITNPayload payload) {
        log.info("Processing successful payment for checkout: {}", checkout.getId());

        try {
            // 1. Update checkout payment status
            checkout.setPaymentStatus(PaymentStatus.COMPLETED);
            checkout.setStatus(CheckoutStatus.COMPLETED);
            checkout.setUpdatedAt(now());
            checkoutRepository.save(checkout);

            // 2. Create order
            Order order = orderCreationService.createOrderFromCheckout(
                    checkout,
                    payload.getPf_payment_id()
            );

            log.info("Order created successfully: {}", order.getId());

            // 3. Send notifications asynchronously
            sendSuccessNotifications(order, checkout);

        } catch (Exception e) {
            log.error("Error handling payment success: {}", e.getMessage(), e);
            // Mark as failed even though payment succeeded
            checkout.setPaymentStatus(PaymentStatus.FAILED);
            checkout.setLastPaymentError("Order creation failed: " + e.getMessage());
            checkoutRepository.save(checkout);
            throw e;
        }
    }

    /**
        * Handle payment failure
     **/
    private void handlePaymentFailure(Checkout checkout, String reason) {
        log.warn("Payment failed for checkout: {}. Reason: {}", checkout.getId(), reason);

        checkout.setPaymentStatus(PaymentStatus.FAILED);
        checkout.setStatus(CheckoutStatus.FAILED);
        checkout.setLastPaymentError(reason);
        checkout.setUpdatedAt(now());

        checkoutRepository.save(checkout);

        // Send failure notification
        sendFailureNotification(checkout);
    }

    /**
     * Verify IP address is from PayFast
     */
    private boolean verifyPayFastIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // In sandbox mode, skip IP verification
        if (payFastConfig.isSandbox()) {
            log.debug("Sandbox mode: skipping IP verification");
            return true;
        }

        boolean isValid = payFastConfig.getValidIps().contains(remoteAddr);
        log.debug("IP verification for {}: {}", remoteAddr, isValid);

        return isValid;
    }

    /**
     * Send success notifications (async)
     */
    @Async
    protected void sendSuccessNotifications(Order order, Checkout checkout) {
        try {
            notificationService.sendOrderConfirmationEmail(order);
            notificationService.sendOrderConfirmationSMS(checkout.getUser().getPhone(), order);
        } catch (Exception e) {
            log.error("Error sending notifications: {}", e.getMessage(), e);
            // Don't throw - notifications are not critical
        }
    }

    /**
     * Send failure notification (async)
     */
    private void sendFailureNotification(Checkout checkout) {
        try {
            notificationService.sendPaymentFailureEmail(checkout);
        } catch (Exception e) {
            log.error("Error sending failure notification: {}", e.getMessage(), e);
        }
    }
}
