package co.za.ecommerce.business;

import co.za.ecommerce.dto.PayFastITNPayload;
import co.za.ecommerce.dto.PayFastPaymentRequest;
import co.za.ecommerce.dto.PaymentInitializationResponse;
import co.za.ecommerce.model.checkout.Checkout;

public interface PayFastService {
    PaymentInitializationResponse initializePayment(Checkout checkout);
    PayFastPaymentRequest buildPaymentRequest(Checkout checkout, String paymentReference);
    String generateSignature(PayFastPaymentRequest request);
    boolean verifySignature(PayFastITNPayload payload);
    boolean verifyPaymentWithPayFast(PayFastITNPayload payload);
}
