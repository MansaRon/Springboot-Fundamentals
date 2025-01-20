package co.za.ecommerce.repository;

import co.za.ecommerce.model.Image;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends MongoRepository<Image, ObjectId> {

}
