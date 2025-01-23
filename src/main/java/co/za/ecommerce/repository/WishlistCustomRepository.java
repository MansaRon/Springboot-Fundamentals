package co.za.ecommerce.repository;

import co.za.ecommerce.model.Wishlist;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface WishlistCustomRepository {
    List<Wishlist> findAllByUserIdOrderByCreatedAtDesc(ObjectId id);
    Optional<Wishlist> findByUserIdAndProductId(ObjectId userId, ObjectId productId);
}
