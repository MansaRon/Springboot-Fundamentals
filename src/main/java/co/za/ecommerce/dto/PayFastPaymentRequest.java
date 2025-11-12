package co.za.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
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
public class PayFastPaymentRequest {
    @NotBlank
    private String merchant_id;
    @NotBlank
    private String merchant_key;
    @NotBlank
    private String return_url;
    @NotBlank
    private String cancel_url;
    @NotBlank
    private String notify_url;
    private String name_first;
    private String name_last;
    private String email_address;
    private String cell_number;
    @NotBlank
    private String m_payment_id;
    @NotBlank
    private String amount;
    @NotBlank
    private String item_name;
    private String item_description;
    private String custom_str1;
    private String custom_int1;
    private String email_confirmation = "0";
    private String confirmation_address;
    private String signature;
}
