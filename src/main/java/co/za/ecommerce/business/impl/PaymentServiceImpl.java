package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.PaymentService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.utils.GenerateID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final Random random = new Random();

    @Override
    public PaymentResultDTO processPayment(Checkout checkout) {
        log.info("=== Simulating Payment Processing ===");
        log.info("Checkout ID: {}", checkout.getId());
        log.info("Amount: R{}", checkout.getTotalAmount());
        log.info("Payment Method: {}", checkout.getPaymentMethod());

        simulateProcessingDelay();

        if (PaymentMethod.CASH_ON_DELIVERY.equals(checkout.getPaymentMethod())) {
            return createSuccessResult(checkout, "PENDING");
        }

        boolean isSuccess = random.nextInt(100) < 70;

        if (isSuccess) {
            return createSuccessResult(checkout, "COMPLETED");
        } else {
            return createFailureResult(checkout);
        }
    }

    @Override
    public PaymentResultDTO createSuccessResult(Checkout checkout, String paymentStatus) {
        String transactionId = GenerateID.generateTransactionId();

        log.info("✅ Payment Successful!");
        log.info("Transaction ID: {}", transactionId);

        return PaymentResultDTO.builder()
                .success(true)
                .transactionId(transactionId)
                .paymentStatus(paymentStatus)
                .amountProcessed(checkout.getTotalAmount())
                .paymentMethod(checkout.getPaymentMethod().toString())
                .updatedAt(LocalDateTime.now())
                .message("Payment processed successfully")
                .build();
    }

    @Override
    public PaymentResultDTO createFailureResult(Checkout checkout) {
        String[] failureReasons = {
                "Insufficient funds",
                "Card declined",
                "Transaction timeout",
                "Invalid card details",
                "Payment gateway error"
        };

        String reason = failureReasons[random.nextInt(failureReasons.length)];

        log.warn("❌ Payment Failed!");
        log.warn("Reason: {}", reason);

        return PaymentResultDTO.builder()
                .success(false)
                .transactionId(null)
                .paymentStatus("FAILED")
                .amountProcessed(0.0)
                .paymentMethod(checkout.getPaymentMethod().toString())
                .updatedAt(LocalDateTime.now())
                .message(reason)
                .failureReason(reason)
                .build();
    }

    @Override
    public void simulateProcessingDelay() {
        try {
            int delay = 500 + random.nextInt(1500);  // 500-2000ms
            Thread.sleep(delay);
            log.debug("Simulated processing delay: {}ms", delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Processing delay interrupted");
        }
    }

    @Override
    public void validatePaymentRequest(Checkout checkout) {
        if (checkout.getTotalAmount() <= 0) {
            throw new PaymentException(
                    "INVALID_AMOUNT",
                    "Payment amount must be greater than zero",
                    400
            );
        }

        if (PaymentMethod.NOT_SELECTED.equals(checkout.getPaymentMethod())) {
            throw new PaymentException(
                    "NO_PAYMENT_METHOD",
                    "Payment method not selected",
                    400
            );
        }
    }
}