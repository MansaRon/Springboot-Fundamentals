package co.za.ecommerce.dto.order;

import co.za.ecommerce.model.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {
    @NotNull(message = "New status is required")
    private OrderStatus newStatus;
    private String notes;
}
