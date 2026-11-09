package co.za.ecommerce.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A product's sales totals, for the admin "top sellers" statistic.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductDTO {
    private String productId;
    private String title;
    private long quantitySold;
    private double revenue;
}
