package co.za.ecommerce.business;

import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.order.OrderItems;

import java.util.List;

public interface InventoryService {
    void reduceInventory(List<CartItems> items);
    void restoreInventory(List<OrderItems> items);
}
