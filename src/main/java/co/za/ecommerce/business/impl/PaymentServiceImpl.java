package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.PaymentService;
import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.PaymentStatus;
import co.za.ecommerce.exception.PaymentException;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.utils.DateUtil;
import co.za.ecommerce.utils.GenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final Random random = new Random();
    private static final String[] FAILURE_REASONS = {
            "Insufficient funds",
            "Card declined",
            "Transaction timeout",
            "Invalid card details",
            "Payment gateway error"
    };

    @Override
    public PaymentResultDTO processPayment(Checkout checkout) {
        log.info("=== Simulating Payment Processing ===");
        log.info("Checkout ID: {}", checkout.getId());
        log.info("Amount: R{}", checkout.getTotalAmount());
        log.info("Payment Method: {}", checkout.getPaymentMethod());

        validatePaymentRequest(checkout);
        simulateProcessingDelay();

        if (PaymentMethod.CASH_ON_DELIVERY.equals(checkout.getPaymentMethod())) {
            return createSuccessResult(checkout, PaymentStatus.PENDING);
        }

        boolean isSuccess = random.nextInt(100) < 70;

        return isSuccess
                ? createSuccessResult(checkout, PaymentStatus.COMPLETED)
                : createFailureResult(checkout);
    }

    private PaymentResultDTO createSuccessResult(Checkout checkout, PaymentStatus paymentStatus) {
        String transactionId = GenerateUtil.generateTransactionId();

        log.info("Payment Successful!");
        log.info("Transaction ID: {}", transactionId);

        return PaymentResultDTO.builder()
                .success(true)
                .transactionId(transactionId)
                .paymentStatus(paymentStatus)
                .amountProcessed(checkout.getTotalAmount())
                .paymentMethod(checkout.getPaymentMethod().toString())
                .updatedAt(DateUtil.now())
                .message("Payment processed successfully")
                .build();
    }

    private PaymentResultDTO createFailureResult(Checkout checkout) {
        String reason = FAILURE_REASONS[random.nextInt(FAILURE_REASONS.length)];
        log.warn("Payment Failed!");
        log.warn("Reason: {}", reason);
        log.warn("Checkout ID: {}", checkout.getId());

        return PaymentResultDTO.builder()
                .success(false)
                .transactionId(null)
                .paymentStatus(PaymentStatus.FAILED)
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
                    "Payment amount must be greater than zero.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (PaymentMethod.NOT_SELECTED.equals(checkout.getPaymentMethod())) {
            throw new PaymentException(
                    "NO_PAYMENT_METHOD",
                    "Payment method not selected.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }
}