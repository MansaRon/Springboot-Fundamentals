package co.za.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin request to refund an order. {@code amount} is optional — a null value refunds the
 * full remaining refundable balance; a value must be positive and cannot exceed that balance.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @Positive(message = "Refund amount must be positive")
    private Double amount;

    @NotBlank(message = "A reason for the refund is required")
    private String reason;
}
