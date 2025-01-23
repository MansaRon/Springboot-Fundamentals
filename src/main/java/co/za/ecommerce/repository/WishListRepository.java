package co.za.ecommerce.repository;

import co.za.ecommerce.model.Wishlist;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishListRepository extends MongoRepository<Wishlist, ObjectId>, WishlistCustomRepository {
}
