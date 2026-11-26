package co.za.ecommerce.dto.coupon;

import co.za.ecommerce.dto.base.EntityDTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Transport for a {@link co.za.ecommerce.model.Coupon}. Doubles as the admin create/update
 * request body — the constraints apply when the controller binds it with {@code @Valid}.
 * {@code usedCount} is read-only (server-managed) and ignored on write.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO extends EntityDTO {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @PositiveOrZero(message = "Minimum spend must be zero or positive")
    private double minSpend;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expiresAt;

    @Positive(message = "Usage limit must be greater than zero")
    private int usageLimit;

    /** Server-managed redemption count — included in responses, ignored on write. */
    private int usedCount;

    private boolean active;
}
