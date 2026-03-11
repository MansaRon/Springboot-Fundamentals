package co.za.ecommerce.dto.otp;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTPResponseDTO {
    private String phoneNumber;
    private Integer expiryMinutes;
    private Boolean valid;
}
