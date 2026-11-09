package co.za.ecommerce.dto.order;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatisticsDTO {
    private int totalOrders;

    // Counts per status.
    private int pendingOrders;
    private int confirmedOrders;
    private int processingOrders;
    private int shippedOrders;
    private int deliveredOrders;
    private int cancelledOrders;
    private int refundedOrders;

    // Revenue metrics — computed over paid, non-cancelled, non-refunded orders.
    private double totalRevenue;
    private double averageOrderValue;

    // Customer metrics.
    private long totalCustomers;

    // Breakdowns for charts.
    private List<TopProductDTO> topSellingProducts;
    private List<DailySalesDTO> salesByDay;
}
