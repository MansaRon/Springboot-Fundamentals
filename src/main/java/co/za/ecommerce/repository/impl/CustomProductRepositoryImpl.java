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
        Query query = new Query();
        query.addCriteria(Criteria.where("category").regex("^" + category + "$", "i")
                .and("deleted").ne(true));
        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class, "products");
        long total = mongoTemplate.count(query.skip(-1).limit(-1), Product.class, "products");

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public Page<Product> findByTitleIgnoreCase(String title, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("title").regex("^" + title + "$", "i")
                .and("deleted").ne(true));
        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class, "products");
        long total = mongoTemplate.count(query.skip(-1).limit(-1), Product.class, "products");

        return new PageImpl<>(products, pageable, total);
    }
}
