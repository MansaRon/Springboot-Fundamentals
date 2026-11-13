package co.za.ecommerce.repository;

import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderStatus;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * All read queries exclude soft-deleted orders ({@code deleted == false}); the
 * stale-order query returns live candidates to soft-delete.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    Optional<Order> findByIdAndDeletedFalse(ObjectId id);
    Optional<Order> findByOrderNumberAndDeletedFalse(String orderNumber);
    List<Order> findByCustomerInfoIdAndDeletedFalseOrderByCreatedAtDesc(ObjectId userId);
    List<Order> findByOrderStatusAndDeletedFalse(OrderStatus status);
    List<Order> findByCustomerInfoIdAndOrderStatusAndDeletedFalse(ObjectId userId, OrderStatus status);
    Optional<Order> findByIdAndCustomerInfoIdAndDeletedFalse(ObjectId orderId, ObjectId customerId);
    // Match "not deleted" as deleted != true rather than == false, so orders created
    // before the soft-delete field existed (field absent in Mongo) are still returned.
    // A Mongo query for { deleted: false } does NOT match documents missing the field.
    @Query("{ 'deleted': { $ne: true } }")
    List<Order> findByDeletedFalse();

    @Query("{ 'deleted': { $ne: true } }")
    Page<Order> findByDeletedFalse(Pageable pageable);

    /** Non-deleted orders with a given status (paginated). */
    @Query("{ 'deleted': { $ne: true }, 'orderStatus': ?0 }")
    Page<Order> findActiveByStatus(OrderStatus status, Pageable pageable);

    /** Non-deleted orders matching the search term (order number, customer name/email, transaction id). */
    @Query("{ 'deleted': { $ne: true }, $or: [ " +
            "{ 'orderNumber': { $regex: ?0, $options: 'i' } }, " +
            "{ 'customerInfo.name': { $regex: ?0, $options: 'i' } }, " +
            "{ 'customerInfo.email': { $regex: ?0, $options: 'i' } }, " +
            "{ 'transactionId': { $regex: ?0, $options: 'i' } } ] }")
    Page<Order> searchActiveOrders(String search, Pageable pageable);

    /** Non-deleted orders matching the search term AND an exact status. */
    @Query("{ 'deleted': { $ne: true }, 'orderStatus': ?1, $or: [ " +
            "{ 'orderNumber': { $regex: ?0, $options: 'i' } }, " +
            "{ 'customerInfo.name': { $regex: ?0, $options: 'i' } }, " +
            "{ 'customerInfo.email': { $regex: ?0, $options: 'i' } }, " +
            "{ 'transactionId': { $regex: ?0, $options: 'i' } } ] }")
    Page<Order> searchActiveOrdersByStatus(String search, OrderStatus status, Pageable pageable);

    /** Live candidates for stale-order cleanup: a given status, created before the cutoff. */
    List<Order> findByOrderStatusAndDeletedFalseAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);
}
