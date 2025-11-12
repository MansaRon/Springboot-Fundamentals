package co.za.ecommerce.repository;

import co.za.ecommerce.model.order.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    Optional<Order> findByTransactionId(String transactionId);
}
