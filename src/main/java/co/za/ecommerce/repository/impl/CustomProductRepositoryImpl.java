package co.za.ecommerce.repository.impl;

import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.CustomProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final MongoTemplate mongoTemplate;

    public CustomProductRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate= mongoTemplate;
    }

    @Override
    public Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable) {
        // Build the query
        Query query = new Query();

        // Add category filter (case-insensitive match)
        query.addCriteria(Criteria.where("category")
                .regex("^" + category + "$", "i"));

        // Set pagination
        query.with(pageable);

        // Execute the query to fetch products
        List<Product> products = mongoTemplate
                .find(query, Product.class, "products");

        // Count total documents for pagination metadata
        long total = mongoTemplate
                .count(query.skip(-1).limit(-1), Product.class, "products");

        // Return results as a Page object
        return new PageImpl<>(products, pageable, total);
    }
}
