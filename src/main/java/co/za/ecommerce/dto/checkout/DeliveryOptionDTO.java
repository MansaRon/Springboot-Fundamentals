package co.za.ecommerce.dto.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A selectable delivery option surfaced to the frontend so it can render real
 * backend-driven methods, prices and ETAs instead of hardcoded values.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOptionDTO {
    /** Stable enum code the frontend sends back as {@code shippingMethod}. */
    private String code;
    private String label;
    private double price;
    private int estimatedDays;
}
