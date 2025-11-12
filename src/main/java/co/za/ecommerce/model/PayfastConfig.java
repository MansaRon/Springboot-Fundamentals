package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "payfast")
public class PayfastConfig {
    /**
     * PayFast merchant ID
     */
    private String merchantId;

    /**
     * PayFast merchant key
     */
    private String merchantKey;

    /**
     * PayFast passphrase for signature generation (optional but recommended)
     */
    private String passphrase;

    /**
     * PayFast payment gateway URL
     */
    private String paymentUrl;

    /**
     * Your webhook URL for ITN (Instant Transaction Notification)
     */
    private String notifyUrl;

    /**
     * Success return URL (where user is redirected after successful payment)
     */
    private String returnUrl;

    /**
     * Cancel URL (where user is redirected if they cancel)
     */
    private String cancelUrl;

    /**
     * Whether to use sandbox mode
     */
    private boolean sandbox = true;

    /**
     * Valid PayFast server IPs (for ITN verification)
     */
    private List<String> validIps = Arrays.asList(
            "197.97.145.144",
            "41.74.179.194",
            "41.74.179.195",
            "41.74.179.196",
            "41.74.179.197",
            "197.97.145.145"
    );

    /**
     * PayFast validation URL
     */
    private String validateUrl;

    /**
     * Get the appropriate payment URL based on sandbox mode
     */
    public String getPaymentUrl() {
        if (sandbox) {
            return "https://sandbox.payfast.co.za/eng/process";
        }
        return "https://www.payfast.co.za/eng/process";
    }

    /**
     * Get the appropriate validation URL based on sandbox mode
     */
    public String getValidateUrl() {
        if (sandbox) {
            return "https://sandbox.payfast.co.za/eng/query/validate";
        }
        return "https://www.payfast.co.za/eng/query/validate";
    }
}
