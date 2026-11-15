package co.za.ecommerce.repository;

import co.za.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProductRepository {
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
    Page<Product> findByTitleIgnoreCase(String title, Pageable pageable);

    /**
     * Paginated catalog query with optional filters. {@code search} is a case-insensitive
     * partial match against title, description and category; {@code category} is a
     * case-insensitive exact match. Either/both may be null/blank to skip that filter.
     */
    Page<Product> searchProducts(String search, String category, Pageable pageable);
}
