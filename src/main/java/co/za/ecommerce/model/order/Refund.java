package co.za.ecommerce.model.order;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

/**
 * A single refund issued against an order, stored as part of the order's refund history.
 * Orders may be refunded in full or in several partial amounts; the order is marked
 * {@link OrderStatus#REFUNDED} once the cumulative refunded amount reaches the total.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {
    private ObjectId id;
    private double amount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime createdAt;

    /** Who issued the refund (currently always the admin that called the endpoint). */
    private String processedBy;
}
