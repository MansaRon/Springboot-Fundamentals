package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * An order-level, fixed-amount discount code. Validity is bounded by an expiry date and a
 * global usage cap; a per-coupon minimum spend gates when it can be applied.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "coupons")
public class Coupon extends Entity {

    /** The code the customer enters (stored uppercase; matched case-insensitively). */
    @Indexed(unique = true)
    private String code;

    /** Fixed amount deducted from the order total when applied. */
    private double amount;

    /** Minimum order subtotal required before the coupon can be used. */
    private double minSpend;

    /** The coupon is invalid once this moment passes. */
    private LocalDateTime expiresAt;

    /** Maximum number of times the coupon may be redeemed across all customers. */
    private int usageLimit;

    /** How many times the coupon has been redeemed so far. */
    private int usedCount;

    /** Only active coupons can be applied. */
    private boolean active;
}
