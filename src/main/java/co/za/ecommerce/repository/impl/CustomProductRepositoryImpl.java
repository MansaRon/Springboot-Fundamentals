package co.za.ecommerce.repository.impl;

import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.CustomProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    @Override
    public Page<Product> findByTitleIgnoreCase(String title, Pageable pageable) {
        // Build the query
        Query query = new Query();

        // Add category filter (case-insensitive match)
        query.addCriteria(Criteria.where("title")
                .regex("^" + title + "$", "i"));

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

    @Override
    public Page<Product> searchProducts(String search, String category, Pageable pageable) {
        List<Criteria> filters = new ArrayList<>();

        if (category != null && !category.isBlank()) {
            // Exact (but case-insensitive) category match.
            filters.add(Criteria.where("category")
                    .regex("^" + Pattern.quote(category.trim()) + "$", "i"));
        }

        if (search != null && !search.isBlank()) {
            // Partial, case-insensitive match across title, description and category.
            String term = Pattern.quote(search.trim());
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(term, "i"),
                    Criteria.where("description").regex(term, "i"),
                    Criteria.where("category").regex(term, "i")
            ));
        }

        Query query = new Query();
        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters.toArray(new Criteria[0])));
        }

        // Count the full match set before applying pagination.
        long total = mongoTemplate.count(query, Product.class, "products");

        query.with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class, "products");

        return new PageImpl<>(products, pageable, total);
    }
}
