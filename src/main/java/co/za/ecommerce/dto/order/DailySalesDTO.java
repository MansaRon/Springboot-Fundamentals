package co.za.ecommerce.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Revenue and order count for a single day, for the admin sales-trend chart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesDTO {
    /** ISO-8601 date, e.g. "2026-11-09". */
    private String date;
    private long orderCount;
    private double revenue;
}
