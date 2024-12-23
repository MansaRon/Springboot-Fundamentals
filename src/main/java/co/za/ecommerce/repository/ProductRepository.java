package co.za.ecommerce.repository;

import co.za.ecommerce.model.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    Page<Product> findByProductKeyWord(String keyword, Pageable pageable);
    // Ignores case sensitive queries
    @Query("{ 'category': { $regex: '^?0$', $options: 'i' } }")
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
}
