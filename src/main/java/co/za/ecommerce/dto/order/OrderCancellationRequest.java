package co.za.ecommerce.dto.order;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional body for a customer order cancellation — carries a free-text reason.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellationRequest {

    @Size(max = 500, message = "Cancellation reason must be 500 characters or fewer")
    private String reason;
}
