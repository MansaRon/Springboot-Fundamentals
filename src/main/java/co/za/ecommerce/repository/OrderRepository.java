package co.za.ecommerce.repository;

import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderStatus;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
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
    List<Order> findByDeletedFalse();
    Page<Order> findByDeletedFalse(Pageable pageable);

    /** Live candidates for stale-order cleanup: a given status, created before the cutoff. */
    List<Order> findByOrderStatusAndDeletedFalseAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);
}
