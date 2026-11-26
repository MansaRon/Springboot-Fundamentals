package co.za.ecommerce.repository;

import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Boolean existsByEmail(String username);
    Boolean existsByPhone(String phone);

    /** Case-insensitive partial match of the search term against name, email or phone. */
    @Query("{ $or: [ " +
            "{ 'name':  { $regex: ?0, $options: 'i' } }, " +
            "{ 'email': { $regex: ?0, $options: 'i' } }, " +
            "{ 'phone': { $regex: ?0, $options: 'i' } } ] }")
    Page<User> searchUsers(String search, Pageable pageable);

    Page<User> findByStatus(AccountStatus status, Pageable pageable);

    /** Search term (name/email/phone) AND an exact account status. */
    @Query("{ $and: [ " +
            "{ 'status': ?1 }, " +
            "{ $or: [ " +
            "{ 'name':  { $regex: ?0, $options: 'i' } }, " +
            "{ 'email': { $regex: ?0, $options: 'i' } }, " +
            "{ 'phone': { $regex: ?0, $options: 'i' } } ] } ] }")
    Page<User> searchUsersByStatus(String search, AccountStatus status, Pageable pageable);
}
