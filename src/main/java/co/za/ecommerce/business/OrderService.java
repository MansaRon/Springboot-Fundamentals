package co.za.ecommerce.business;

import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.OrderStatus;
import org.bson.types.ObjectId;

import java.util.List;

public interface OrderService {
    OrderDTO createOrderFromCheckout(Checkout checkout, PaymentResultDTO paymentResultDTO);

    OrderDTO getOrderById(ObjectId orderId);
    OrderDTO getOrderByOrderNumber(String orderNumber);
    List<OrderDTO> getUserOrders(ObjectId userId);
    List<OrderDTO> getUserOrdersByStatus(ObjectId userId, OrderStatus status);

    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByStatus(OrderStatus status);
    OrderDTO updateOrderStatus(ObjectId orderId, OrderStatus newStatus, String notes);
}
