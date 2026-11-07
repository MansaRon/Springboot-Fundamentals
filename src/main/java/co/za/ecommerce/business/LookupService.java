package co.za.ecommerce.business;

import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.model.lookup.LookupType;
import org.bson.types.ObjectId;

import java.util.List;

public interface LookupService {
    List<LookupValueDTO> getActiveByType(LookupType type);
    LookupValueDTO create(LookupValueDTO dto);
    LookupValueDTO update(ObjectId id, LookupValueDTO dto);
    void delete(ObjectId id);
}
