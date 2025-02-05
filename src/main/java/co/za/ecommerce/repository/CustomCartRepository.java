package co.za.ecommerce.repository;

import co.za.ecommerce.model.Cart;
import org.bson.types.ObjectId;

import java.util.Optional;

public interface CustomCartRepository {
    Optional<Cart> findByUserId(ObjectId userId);
}
