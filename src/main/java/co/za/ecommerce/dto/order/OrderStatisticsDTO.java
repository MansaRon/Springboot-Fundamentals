package co.za.ecommerce.dto.order;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatisticsDTO {
    private int totalOrders;
    private int pendingOrders;
    private int confirmedOrders;
    private int processingOrders;
    private int shippedOrders;
    private int deliveredOrders;
    private int cancelledOrders;
}
