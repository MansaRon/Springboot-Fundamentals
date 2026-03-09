package co.za.ecommerce.model.order;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {
    private OrderStatus status;
    private LocalDateTime timestamp;
    private String notes;
    private String updatedBy;
}
