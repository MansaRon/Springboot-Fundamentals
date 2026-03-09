package co.za.ecommerce.repository;

import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerInfo(User user);
    List<Order> findByCustomerInfoId(ObjectId userId);
    List<Order> findByOrderStatus(OrderStatus status);
    List<Order> findByCustomerInfoIdAndOrderStatus(ObjectId userId, OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findTop10ByOrderByOrderDateDesc();
    long countByOrderStatus(OrderStatus status);
}
