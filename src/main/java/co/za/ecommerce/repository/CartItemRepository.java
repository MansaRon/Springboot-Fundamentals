package co.za.ecommerce.repository;

import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends MongoRepository<CartItems, ObjectId>, CustomCartItemRepository {
}
