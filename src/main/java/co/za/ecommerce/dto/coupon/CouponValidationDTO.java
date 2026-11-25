package co.za.ecommerce.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Result of previewing a coupon against a checkout — non-throwing so the frontend can show
 * inline feedback. {@code valid} tells whether it can be applied; {@code discountAmount} is
 * what would be deducted; {@code reason} explains why it's invalid (null when valid).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationDTO {
    private boolean valid;
    private String code;
    private double discountAmount;
    private String reason;
}
