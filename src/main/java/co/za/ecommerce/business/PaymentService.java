package co.za.ecommerce.business;

import co.za.ecommerce.dto.order.PaymentDTO;
import co.za.ecommerce.dto.order.PaymentResultsDTO;

public interface PaymentService {
    /**
     * Process a payment for an order
     * @param amount The total amount to be charged
     * @param paymentDetails The payment details including method, currency, etc.
     * @param userId The ID of the user making the payment
     * @return PaymentResultsDTO containing the result of the payment processing
     */
    PaymentResultsDTO processPayment(double amount, PaymentDTO paymentDetails, String userId);
} 