package co.za.ecommerce.dto.order;

import co.za.ecommerce.dto.base.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemsDTO extends EntityDTO {
    private String productId;
    private String productTitle;
    private List<String> imageUrls;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double discount;
    private double tax;
}
