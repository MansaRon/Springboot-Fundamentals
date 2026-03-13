package co.za.ecommerce.business;

import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.model.checkout.Checkout;

public interface PaymentService {
    PaymentResultDTO processPayment(Checkout checkout);
    void simulateProcessingDelay();
    void validatePaymentRequest(Checkout checkout);
}
