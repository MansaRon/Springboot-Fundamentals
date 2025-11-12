package co.za.ecommerce.business;

import co.za.ecommerce.dto.PayFastITNPayload;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentWebhookService {
    void processPayFastITN(PayFastITNPayload payload, HttpServletRequest request);
}
