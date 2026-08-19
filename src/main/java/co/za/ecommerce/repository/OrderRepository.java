package co.za.ecommerce.repository;

import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {

    @Override
    @Query("{ '_id': ?0, 'deleted': { $ne: true } }")
    Optional<Order> findById(ObjectId id);

    @Override
    @Query("{ 'deleted': { $ne: true } }")
    List<Order> findAll();

    @Query("{ 'orderNumber': ?0, 'deleted': { $ne: true } }")
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("{ 'customerInfo': ?0, 'deleted': { $ne: true } }")
    List<Order> findByCustomerInfo(User user);

    @Query("{ 'customerInfo._id': ?0, 'deleted': { $ne: true } }")
    List<Order> findByCustomerInfoId(ObjectId userId);

    @Query("{ 'orderStatus': ?0, 'deleted': { $ne: true } }")
    List<Order> findByOrderStatus(OrderStatus status);

    @Query("{ 'customerInfo._id': ?0, 'orderStatus': ?1, 'deleted': { $ne: true } }")
    List<Order> findByCustomerInfoIdAndOrderStatus(ObjectId userId, OrderStatus status);

    @Query("{ '_id': ?0, 'customerInfo._id': ?1, 'deleted': { $ne: true } }")
    Optional<Order> findByIdAndCustomerInfoId(ObjectId orderId, ObjectId customerId);

    @Query(value = "{ 'orderStatus': ?0, 'deleted': { $ne: true } }", count = true)
    long countByOrderStatus(OrderStatus status);
}
