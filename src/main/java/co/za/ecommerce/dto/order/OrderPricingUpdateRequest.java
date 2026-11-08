package co.za.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin request to adjust an order's pricing. {@code discount} and {@code shippingCost}
 * are optional — a null value leaves the existing amount unchanged (an explicit {@code 0}
 * clears it). The order total is recalculated server-side from these.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPricingUpdateRequest {

    @PositiveOrZero(message = "Discount must be zero or positive")
    private Double discount;

    @PositiveOrZero(message = "Shipping cost must be zero or positive")
    private Double shippingCost;

    @NotBlank(message = "A reason for the adjustment is required")
    private String reason;
}
