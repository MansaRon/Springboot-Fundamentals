package co.za.ecommerce.repository;

import co.za.ecommerce.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Boolean existsByEmail(String username);
    Boolean existsByPhone(String phone);
}
