package co.za.ecommerce.repository.impl;

import co.za.ecommerce.model.Wishlist;
import co.za.ecommerce.repository.WishlistCustomRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomWishlistRepositoryImpl implements WishlistCustomRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Wishlist> findAllByUserIdOrderByCreatedAtDesc(ObjectId id) {
        return mongoTemplate.find(
                Query.query(Criteria.where("userId").is(id))
                        .with(Sort.by(Sort.Direction.DESC, "createdAt")),
                Wishlist.class
        );
    }

    @Override
    public Optional<Wishlist> findByUserIdAndProductId(ObjectId userId, ObjectId productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("productId").is(productId));
        Wishlist wishlist = mongoTemplate.findOne(query, Wishlist.class);
        return Optional.ofNullable(wishlist);
    }
}
