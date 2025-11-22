package co.za.ecommerce.api.impl;

import co.za.ecommerce.dto.PayFastITNPayload;
import co.za.ecommerce.dto.PaymentCancelDTO;
import co.za.ecommerce.dto.api.*;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.mapper.CheckoutMapper;
import co.za.ecommerce.model.checkout.Checkout;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payments")
public class PaymentAPIImpl extends API {

    /**
     * Initialize payment for a checkout
     *
     * Endpoint: POST /api/v1/payments/initialize/{checkoutId}
     * Called when: User clicks "Pay Now" button
     * Returns: Payment URL to redirect user to PayFast
     *
     * @param checkoutId The ID of the checkout to process payment for
     * @return PaymentInitializationApiResource containing payment URL
     */
    @PermitAll
    @PostMapping("/initialize/{checkoutId}")
    public ResponseEntity<PaymentInitializationApiResource> initializePayment(
            @PathVariable ObjectId checkoutId) {

        log.info("Payment initialization requested for checkout: {}", checkoutId);

        return ResponseEntity.ok(
                PaymentInitializationApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.initializePayment(checkoutId))
                        .message("Payment initialized. Redirect user to payment URL...")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * PayFast ITN (Instant Transaction Notification) webhook
     *
     * Endpoint: POST /api/v1/payments/payfast/notify
     * Called by: PayFast servers after payment is processed
     * Purpose: Verify payment and create order
     *
     * CRITICAL: Must return 200 OK within 10 seconds
     *
     * @param payload Payment details from PayFast
     * @param request HTTP request for IP verification
     * @return 200 OK (always, even on error to prevent retries)
     */
    @PostMapping("/payfast/notify")
    public ResponseEntity<Void> receivePayFastNotification(
            @Valid @RequestBody PayFastITNPayload payload,
            HttpServletRequest request) {

        log.info("=== PayFast ITN Received ===");
        log.info("Payment ID: {}", payload.getM_payment_id());

        try {
            webhookService.processPayFastITN(payload, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing PayFast ITN: {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Payment success callback
     *
     * Endpoint: GET /api/v1/payments/success?payment_id=CHK-xxx
     * Called when: User is redirected back from PayFast after successful payment
     * Purpose: Show user success message (order created via webhook)
     *
     * Note: This is for user feedback only. The ITN webhook is the source of truth.
     *
     * @param paymentRequestId The payment reference ID
     * @return PaymentSuccessApiResource with checkout details
     */
    @GetMapping("/success")
    public ResponseEntity<PaymentResultDTOApiResource> handlePaymentSuccess(
            @RequestParam("payment_id") String paymentRequestId) {
        log.info("Payment success callback for: {}", paymentRequestId);

        return ResponseEntity.ok(
                PaymentResultDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.getCheckoutDTOByPaymentRequestId(paymentRequestId))
                        .message("Payment successful! Your order is being processed.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Payment cancellation callback
     *
     * Endpoint: GET /api/v1/payments/cancel?payment_id=CHK-xxx
     * Called when: User clicks "Cancel" on PayFast payment page
     * Purpose: Update checkout status and show cancellation message
     *
     * @param paymentRequestId The payment reference ID
     * @return PaymentCancelDTOApiResource with cancellation details
     */
    @GetMapping("/cancel")
    public ResponseEntity<PaymentCancelDTOApiResource> handlePaymentCancellation(
            @RequestParam("payment_id") String paymentRequestId) {
        checkoutService.handlePaymentCancellation(paymentRequestId);

        return ResponseEntity.ok(
                PaymentCancelDTOApiResource.builder()
                        .timestamp(now())
                        .data(PaymentCancelDTO.builder()
                                .cancelled(true)
                                .paymentRequestId(paymentRequestId)
                                .message("Payment was cancelled. You can try again anytime.")
                                .build())
                        .message("Payment cancellation processed.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * Check payment/checkout status
     *
     * Endpoint: GET /api/v1/payments/status/{checkoutId}
     * Called by: Frontend polling after user returns from PayFast
     * Purpose: Check if payment is completed and order is created
     *
     * Frontend should poll this endpoint every 2-3 seconds for up to 30 seconds
     * after user returns from PayFast payment page.
     *
     * @param checkoutId The checkout ID to check status for
     * @return CheckoutStatusApiResource with payment status
     */
    @GetMapping("/status/{checkoutId}")
    public ResponseEntity<CheckoutStatusApiResource> getPaymentStatus(
            @PathVariable String checkoutId) {
        log.info("Payment status check for checkout: {}", checkoutId);
        return ResponseEntity.ok(
                CheckoutStatusApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.getCheckoutStatus(new ObjectId(checkoutId)))
                        .message("Payment status retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

}
