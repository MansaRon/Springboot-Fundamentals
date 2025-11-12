package co.za.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitializationResponse {
    /**
     * Full PayFast payment URL with all parameters
     * Redirect user to this URL to complete payment
     */
    private String paymentUrl;

    /**
     * Your internal payment reference (m_payment_id)
     * Used to track this payment
     */
    private String paymentRequestId;

    /**
     * The checkout ID this payment is for
     */
    private String checkoutId;

    /**
     * Total amount to be paid
     */
    private double amount;

    /**
     * MD5 signature for verification
     */
    private String signature;
}
