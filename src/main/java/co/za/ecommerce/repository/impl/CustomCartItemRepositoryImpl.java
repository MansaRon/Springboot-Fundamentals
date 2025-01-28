package co.za.ecommerce.repository.impl;

import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.CustomCartItemRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CustomCartItemRepositoryImpl implements CustomCartItemRepository {

    private final MongoTemplate mongoTemplate;

    public CustomCartItemRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate= mongoTemplate;
    }

    @Override
    public Product findProductById(ObjectId productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("product.id").is(productId));
        query.fields().include("product");

        CartItems cartItem = mongoTemplate.findOne(query, CartItems.class);
        return cartItem != null ? cartItem.getProduct() : null;
    }

    @Override
    public CartItems findCartItemsByProductIdAndCartId(ObjectId cartId, ObjectId productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("cart.id").is(cartId)
                .and("product.id").is(productId));
        return mongoTemplate.findOne(query, CartItems.class);
    }

    @Override
    public void deleteCartItemByProductIdAndCartId(ObjectId cartId, ObjectId productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("cart.id").is(cartId)
                .and("product.id").is(productId));
        mongoTemplate.remove(query, CartItems.class);
    }
}
