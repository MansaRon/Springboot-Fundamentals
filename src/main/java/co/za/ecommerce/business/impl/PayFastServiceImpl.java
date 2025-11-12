package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.PayFastService;
import co.za.ecommerce.dto.PayFastITNPayload;
import co.za.ecommerce.dto.PayFastPaymentRequest;
import co.za.ecommerce.dto.PaymentInitializationResponse;
import co.za.ecommerce.model.PayfastConfig;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayFastServiceImpl implements PayFastService {

    private final PayfastConfig config;
    private final RestTemplate restTemplate;

    @Override
    public PaymentInitializationResponse initializePayment(Checkout checkout) {
        log.info("Initializing PayFast payment for checkout: {}", checkout.getId());

        // Generate unique payment reference
        String paymentReference = generatePaymentReference(checkout);

        // Build PayFast request
        PayFastPaymentRequest request = buildPaymentRequest(checkout, paymentReference);

        // Generate signature
        String signature = generateSignature(request);
        request.setSignature(signature);

        // Build payment URL with query parameters
        String paymentUrl = buildPaymentUrl(request);

        log.info("Payment URL generated for checkout {}: {}", checkout.getId(), paymentUrl);

        return PaymentInitializationResponse.builder()
                .paymentUrl(paymentUrl)
                .paymentRequestId(paymentReference)
                .checkoutId(checkout.getId().toString())
                .amount(checkout.getTotalAmount())
                .signature(signature)
                .build();
    }

    @Override
    public PayFastPaymentRequest buildPaymentRequest(Checkout checkout, String paymentReference) {
        User user = checkout.getUser();

        return PayFastPaymentRequest.builder()
                .merchant_id(config.getMerchantId())
                .merchant_key(config.getMerchantKey())
                .return_url(config.getReturnUrl() + "?payment_id=" + paymentReference)
                .cancel_url(config.getCancelUrl() + "?payment_id=" + paymentReference)
                .notify_url(config.getNotifyUrl())
                .m_payment_id(paymentReference)
                .amount(String.format("%.2f", checkout.getTotalAmount()))
                .item_name("Order #" + checkout.getId())
                .item_description(buildItemDescription(checkout))
                .name_first(user.getName().split(" ")[0])
                .name_last(user.getName().split(" ").length > 1
                        ? user.getName().split(" ")[1] : "")
                .email_address(user.getEmail())
                .cell_number(user.getPhone())
                .custom_str1(checkout.getId().toString())
                .email_confirmation("0")
                .build();
    }

    @Override
    public String generateSignature(PayFastPaymentRequest request) {
        Map<String, String> params = new TreeMap<>();

        params.put("merchant_id", request.getMerchant_id());
        params.put("merchant_key", request.getMerchant_key());
        params.put("return_url", request.getReturn_url());
        params.put("cancel_url", request.getCancel_url());
        params.put("notify_url", request.getNotify_url());
        params.put("name_first", request.getName_first());
        params.put("name_last", request.getName_last());
        params.put("email_address", request.getEmail_address());
        params.put("cell_number", request.getCell_number());
        params.put("m_payment_id", request.getM_payment_id());
        params.put("amount", request.getAmount());
        params.put("item_name", request.getItem_name());
        params.put("item_description", request.getItem_description());

        if (request.getCustom_str1() != null) {
            params.put("custom_str1", request.getCustom_str1());
        }
        if (request.getCustom_int1() != null) {
            params.put("custom_int1", request.getCustom_int1());
        }

        // Build parameter string
        String paramString = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> {
                    return URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(e.getValue().trim(), StandardCharsets.UTF_8);
                })
                .collect(Collectors.joining("&"));

        // Add passphrase if configured
        if (config.getPassphrase() != null && !config.getPassphrase().isEmpty()) {
            paramString += "&passphrase=" + URLEncoder.encode(config.getPassphrase(), StandardCharsets.UTF_8);
        }

        log.debug("Signature string: {}", paramString);

        return generateMD5(paramString);
    }

    @Override
    public boolean verifySignature(PayFastITNPayload payload) {
        Map<String, String> params = new TreeMap<>();

        params.put("m_payment_id", payload.getM_payment_id());
        params.put("pf_payment_id", payload.getPf_payment_id());
        params.put("payment_status", payload.getPayment_status());
        params.put("item_name", payload.getItem_name());
        params.put("item_description", payload.getItem_description());
        params.put("amount_gross", payload.getAmount_gross());
        params.put("amount_fee", payload.getAmount_fee());
        params.put("amount_net", payload.getAmount_net());

        if (payload.getCustom_str1() != null) params.put("custom_str1", payload.getCustom_str1());
        if (payload.getName_first() != null) params.put("name_first", payload.getName_first());
        if (payload.getName_last() != null) params.put("name_last", payload.getName_last());
        if (payload.getEmail_address() != null) params.put("email_address", payload.getEmail_address());
        if (payload.getMerchant_id() != null) params.put("merchant_id", payload.getMerchant_id());

        String paramString = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> {
                    return URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(e.getValue().trim(), StandardCharsets.UTF_8);
                })
                .collect(Collectors.joining("&"));

        if (config.getPassphrase() != null && !config.getPassphrase().isEmpty()) {
            paramString += "&passphrase=" + URLEncoder.encode(config.getPassphrase(), StandardCharsets.UTF_8);
        }

        String calculatedSignature = generateMD5(paramString);
        boolean isValid = calculatedSignature.equals(payload.getSignature());

        log.info("Signature verification: {} (calculated: {}, received: {})",
                isValid, calculatedSignature, payload.getSignature());

        return isValid;
    }

    @Override
    public boolean verifyPaymentWithPayFast(PayFastITNPayload payload) {
        try {
            log.info("Verifying payment with PayFast API for payment_id: {}", payload.getPf_payment_id());

            // Build validation request
            String validationUrl = config.getValidateUrl();

            Map<String, String> params = new HashMap<>();
            params.put("m_payment_id", payload.getM_payment_id());
            params.put("pf_payment_id", payload.getPf_payment_id());
            params.put("amount_gross", payload.getAmount_gross());

            // Send validation request to PayFast
            String response = restTemplate.postForObject(validationUrl, params, String.class);

            boolean isValid = "VALID".equalsIgnoreCase(response);
            log.info("PayFast validation response: {}", response);

            return isValid;
        } catch (Exception e) {
            log.error("Error verifying payment with PayFast: {}", e.getMessage(), e);
            return false;
        }
    }

    private String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private String buildPaymentUrl(PayFastPaymentRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(config.getPaymentUrl())
                .queryParam("merchant_id", request.getMerchant_id())
                .queryParam("merchant_key", request.getMerchant_key())
                .queryParam("return_url", request.getReturn_url())
                .queryParam("cancel_url", request.getCancel_url())
                .queryParam("notify_url", request.getNotify_url())
                .queryParam("name_first", request.getName_first())
                .queryParam("name_last", request.getName_last())
                .queryParam("email_address", request.getEmail_address())
                .queryParam("cell_number", request.getCell_number())
                .queryParam("m_payment_id", request.getM_payment_id())
                .queryParam("amount", request.getAmount())
                .queryParam("item_name", request.getItem_name())
                .queryParam("item_description", request.getItem_description())
                .queryParam("email_confirmation", request.getEmail_confirmation())
                .queryParam("signature", request.getSignature());

        if (request.getCustom_str1() != null) {
            builder.queryParam("custom_str1", request.getCustom_str1());
        }

        return builder.toUriString();
    }

    private String generatePaymentReference(Checkout checkout) {
        return "CHK-" + checkout.getId().toString() + "-" + System.currentTimeMillis();
    }

    private String buildItemDescription(Checkout checkout) {
        return checkout.getItems().stream()
                .limit(3)
                .map(item -> item.getProduct().getTitle())
                .collect(Collectors.joining(", "));
    }
}
