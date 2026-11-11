package co.za.ecommerce.business;

import co.za.ecommerce.dto.PaymentResultDTO;
import co.za.ecommerce.dto.order.GetAllOrdersDTO;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderStatisticsDTO;
import co.za.ecommerce.dto.order.OrderTrackingDTO;
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
    GetAllOrdersDTO getAllOrders(int pageNo, int pageSize, String sortBy, String sortDir);
    List<OrderDTO> getOrdersByStatus(OrderStatus status);
    OrderDTO updateOrderStatus(ObjectId orderId, OrderStatus newStatus, String notes);
    OrderDTO adjustOrderPricing(ObjectId orderId, Double discount, Double shippingCost, String reason);
    int deleteStaleOrders(int olderThanDays);
    OrderStatisticsDTO getOrderStatistics();
    OrderDTO rebookOrder(ObjectId orderId);
    OrderDTO getOrderByIdAndCustomerId(ObjectId orderId, ObjectId customerId);
    OrderTrackingDTO trackOrder(ObjectId customerId, ObjectId orderId);
    OrderDTO cancelOrder(ObjectId customerId, ObjectId orderId, String reason);
}
