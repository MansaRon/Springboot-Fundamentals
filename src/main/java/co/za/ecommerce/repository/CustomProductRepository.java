package co.za.ecommerce.repository;

import co.za.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProductRepository {
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
    Page<Product> findByTitleIgnoreCase(String title, Pageable pageable);
}
