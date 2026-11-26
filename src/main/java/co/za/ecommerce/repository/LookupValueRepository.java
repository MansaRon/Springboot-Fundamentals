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

    /** All values for a type (active and inactive) — for admin management. */
    List<LookupValue> findByTypeOrderBySortOrderAsc(LookupType type);

    /** Whether any value (active or not) exists for a type — used to make seeding idempotent per type. */
    boolean existsByType(LookupType type);

    /** Duplicate guard: a value with the same code already exists within the type (case-insensitive). */
    boolean existsByTypeAndCodeIgnoreCase(LookupType type, String code);
}
