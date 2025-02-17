package co.za.ecommerce.business;

import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.order.Order;
import org.bson.types.ObjectId;

public interface OrderService {
    OrderDTO createOrder(ObjectId userId, Order order);
}
