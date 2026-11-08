package co.za.ecommerce.repository;

import co.za.ecommerce.model.lookup.LookupType;
import co.za.ecommerce.model.lookup.LookupValue;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LookupValueRepository extends MongoRepository<LookupValue, ObjectId> {

    /** Active values for a type, ordered for display. */
    List<LookupValue> findByTypeAndActiveTrueOrderBySortOrderAsc(LookupType type);

    /** All values (active and inactive) for a type, ordered for display — admin management view. */
    List<LookupValue> findByTypeOrderBySortOrderAsc(LookupType type);
}
