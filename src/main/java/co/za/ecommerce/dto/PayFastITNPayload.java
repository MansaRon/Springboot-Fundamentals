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
public class PayFastITNPayload {
    private String m_payment_id;
    private String pf_payment_id;
    private String payment_status;
    private String item_name;
    private String item_description;
    private String amount_gross;
    private String amount_fee;
    private String amount_net;

    private String custom_str1;
    private String custom_str2;
    private String custom_str3;
    private String custom_str4;
    private String custom_str5;

    private String custom_int1;
    private String custom_int2;
    private String custom_int3;
    private String custom_int4;
    private String custom_int5;

    private String name_first;
    private String name_last;
    private String email_address;
    private String merchant_id;
    private String signature;
}
