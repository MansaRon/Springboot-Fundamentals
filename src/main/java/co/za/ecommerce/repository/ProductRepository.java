package co.za.ecommerce.repository;

import co.za.ecommerce.model.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, ObjectId>, CustomProductRepository {

    @Override
    @Query("{ '_id': ?0, 'deleted': { $ne: true } }")
    Optional<Product> findById(ObjectId id);

    @Override
    @Query("{ 'deleted': { $ne: true } }")
    List<Product> findAll();

    @Override
    @Query("{ 'deleted': { $ne: true } }")
    Page<Product> findAll(Pageable pageable);
}
