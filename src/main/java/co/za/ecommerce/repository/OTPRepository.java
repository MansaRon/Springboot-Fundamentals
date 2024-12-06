package co.za.ecommerce.repository;

import co.za.ecommerce.model.OtpStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends MongoRepository<OtpStore, String> {
    Optional<OtpStore> findByPhoneNumber(String id);
    void deleteById(String id);
}
