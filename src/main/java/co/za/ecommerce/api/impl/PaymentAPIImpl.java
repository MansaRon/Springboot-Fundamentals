package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.PaymentWebhookService;
import co.za.ecommerce.dto.PayFastITNPayload;
import co.za.ecommerce.dto.PaymentCancelResponse;
import co.za.ecommerce.dto.PaymentInitializationResponse;
import co.za.ecommerce.dto.PaymentSuccessResponse;
import co.za.ecommerce.dto.checkout.CheckoutStatusDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.CheckoutException;
import co.za.ecommerce.exception.OrderException;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payments")
public class PaymentAPIImpl extends API {
    private final PaymentWebhookService webhookService;
    private final OrderRepository orderRepository;

    /**
     * Initialize payment for a checkout
     * Frontend calls this endpoint when user clicks "Pay Now"
     */
    @PostMapping("/initialize/{checkoutId}")
    public ResponseEntity<PaymentInitializationResponse> initializePayment(
            @PathVariable String checkoutId) {

        log.info("Payment initialization requested for checkout: {}", checkoutId);

        try {
            ObjectId objectId = new ObjectId(checkoutId);
            PaymentInitializationResponse response = checkoutService.initializePayment(objectId);

            return ResponseEntity.ok(response);
        } catch (CheckoutException e) {
            log.error("Checkout error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error initializing payment: {}", e.getMessage(), e);
            throw new PaymentException(
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "Failed to initialize payment",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    /**
     * PayFast ITN (Instant Transaction Notification) webhook
     * CRITICAL: This must be fast and return 200 OK within 10 seconds
     */
    @PostMapping("/payfast/notify")
    public ResponseEntity<Void> receivePayFastNotification(
            @Valid @RequestBody PayFastITNPayload payload,
            HttpServletRequest request) {

        log.info("=== PayFast ITN Received ===");
        log.info("Payment ID: {}", payload.getM_payment_id());

        try {
            // Process the webhook
            webhookService.processPayFastITN(payload, request);

            // MUST return 200 OK to PayFast
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing PayFast ITN: {}", e.getMessage(), e);
            // Still return 200 to prevent PayFast retries
            // Log the error for manual investigation
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Handle successful payment return URL
     * User is redirected here after completing payment on PayFast
     */
    @GetMapping("/success")
    public ResponseEntity<PaymentSuccessResponse> handlePaymentSuccess(
            @RequestParam("payment_id") String paymentRequestId) {

        log.info("Payment success callback for: {}", paymentRequestId);

        try {
            Checkout checkout = checkoutService.getCheckoutByPaymentRequestId(paymentRequestId);

            // Note: Don't rely on this for order creation
            // The ITN webhook is the source of truth
            // This is just for displaying status to the user

            PaymentSuccessResponse response = PaymentSuccessResponse.builder()
                    .success(true)
                    .checkoutId(checkout.getId().toString())
                    .paymentStatus(checkout.getPaymentStatus().toString())
                    .message("Payment successful! Your order is being processed.")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error handling payment success: {}", e.getMessage(), e);
            return ResponseEntity.ok(PaymentSuccessResponse.builder()
                    .success(false)
                    .message("Payment processing. Please check your email for confirmation.")
                    .build());
        }
    }

    /**
     * Handle payment cancellation
     * User is redirected here if they cancel on PayFast
     */
    @GetMapping("/cancel")
    public ResponseEntity<PaymentCancelResponse> handlePaymentCancellation(
            @RequestParam("payment_id") String paymentRequestId) {

        log.info("Payment cancellation for: {}", paymentRequestId);

        try {
            checkoutService.handlePaymentCancellation(paymentRequestId);

            PaymentCancelResponse response = PaymentCancelResponse.builder()
                    .cancelled(true)
                    .message("Payment was cancelled. You can try again anytime.")
                    .paymentRequestId(paymentRequestId)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error handling cancellation: {}", e.getMessage(), e);
            return ResponseEntity.ok(PaymentCancelResponse.builder()
                    .cancelled(true)
                    .message("Payment cancelled.")
                    .build());
        }
    }

    /**
     * Check payment/checkout status
     * Frontend can poll this endpoint to check payment completion
     */
    @GetMapping("/status/{checkoutId}")
    public ResponseEntity<CheckoutStatusDTO> getPaymentStatus(
            @PathVariable String checkoutId) {

        log.info("Payment status check for checkout: {}", checkoutId);

        try {
            ObjectId objectId = new ObjectId(checkoutId);
            CheckoutStatusDTO status = checkoutService.getCheckoutStatus(objectId);
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            throw new CheckoutException(
                    HttpStatus.NOT_FOUND.toString(),
                    "Checkout not found",
                    HttpStatus.NOT_FOUND.value()
            );
        }
    }

    /**
     * Get order by payment request ID (after successful payment)
     */
    @GetMapping("/order/{paymentRequestId}")
    public ResponseEntity<OrderDTO> getOrderByPaymentRequest(
            @PathVariable String paymentRequestId) {

        log.info("Fetching order for payment request: {}", paymentRequestId);

        try {
            Checkout checkout = checkoutService.getCheckoutByPaymentRequestId(paymentRequestId);

            if (!PaymentStatus.COMPLETED.equals(checkout.getPaymentStatus())) {
                throw new PaymentException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Payment not yet completed",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // Find order by transaction ID or checkout reference
            Order order = orderRepository.findByTransactionId(paymentRequestId)
                    .orElseThrow(() -> new OrderException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Order not found",
                            HttpStatus.NOT_FOUND.value()
                    ));

            OrderDTO orderDTO = mapToOrderDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (Exception e) {
            log.error("Error fetching order: {}", e.getMessage(), e);
            throw e;
        }
    }

    private OrderDTO mapToOrderDTO(Order order) {
        // Map Order to OrderDTO
        // Use your existing mapper or implement manually
        return new OrderDTO(); // TODO: Implement mapping
    }
}
