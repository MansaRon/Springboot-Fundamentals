package co.za.ecommerce.repository;

import co.za.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

public interface CustomProductRepository {
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
}
