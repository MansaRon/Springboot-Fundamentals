package co.za.ecommerce.repository;

import co.za.ecommerce.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    @Override
    @Query("{ '_id': ?0, 'deleted': { $ne: true } }")
    Optional<User> findById(ObjectId id);

    @Query("{ 'email': ?0, 'deleted': { $ne: true } }")
    Optional<User> findByEmail(String email);

    @Query("{ 'phone': ?0, 'deleted': { $ne: true } }")
    Optional<User> findByPhone(String phone);

    @Query(value = "{ 'email': ?0, 'deleted': { $ne: true } }", exists = true)
    Boolean existsByEmail(String email);

    @Query(value = "{ 'phone': ?0, 'deleted': { $ne: true } }", exists = true)
    Boolean existsByPhone(String phone);
}
