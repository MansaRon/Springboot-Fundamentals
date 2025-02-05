package co.za.ecommerce.repository.impl;

import co.za.ecommerce.model.Cart;
import co.za.ecommerce.repository.CustomCartRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CustomCartRepositoryImpl implements CustomCartRepository {

    private final MongoTemplate mongoTemplate;

    public CustomCartRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate= mongoTemplate;
    }

    @Override
    public Cart findByUserId(ObjectId userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("user._id").is(userId));
        return mongoTemplate.findOne(query, Cart.class);
    }
}
