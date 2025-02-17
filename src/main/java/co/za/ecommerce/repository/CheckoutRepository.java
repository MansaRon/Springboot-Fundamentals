package co.za.ecommerce.repository;

import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckoutRepository extends MongoRepository<Checkout, ObjectId> {
    Optional<Checkout> findFirstByUserId(ObjectId userId);
    Optional<Checkout> findByCartId(ObjectId cartId);
    List<Checkout> findAllByStatus(CheckoutStatus status);
    void deleteByUserId(ObjectId userId);
}
