package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.LookupService;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.exception.LookupException;
import co.za.ecommerce.mapper.LookupMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.lookup.LookupType;
import co.za.ecommerce.model.lookup.LookupValue;
import co.za.ecommerce.repository.LookupValueRepository;
import co.za.ecommerce.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LookupServiceImpl implements LookupService {

    private final LookupValueRepository lookupValueRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<LookupValueDTO> getActiveByType(LookupType type) {
        log.info("Fetching active lookup values for type: {}", type);
        return LookupMapper.toDTOList(
                lookupValueRepository.findByTypeAndActiveTrueOrderBySortOrderAsc(type));
    }

    @Override
    public List<LookupValueDTO> getAllByType(LookupType type) {
        log.info("Fetching all lookup values (active + inactive) for type: {}", type);
        return LookupMapper.toDTOList(
                lookupValueRepository.findByTypeOrderBySortOrderAsc(type));
    }

    @Override
    public LookupValueDTO create(LookupValueDTO dto) {
        log.info("Creating lookup value: type={}, code={}", dto.getType(), dto.getCode());

        // Reject a duplicate code within the same type (case-insensitive).
        if (lookupValueRepository.existsByTypeAndCodeIgnoreCase(dto.getType(), dto.getCode())) {
            throw new LookupException(
                    HttpStatus.CONFLICT.toString(),
                    String.format("A %s value with code '%s' already exists.", dto.getType(), dto.getCode()),
                    HttpStatus.CONFLICT.value());
        }

        // New values are active by default unless the caller explicitly sent active=false.
        if (dto.getActive() == null) {
            dto.setActive(true);
        }

        LookupValue saved = lookupValueRepository.save(LookupMapper.toEntity(dto));
        return LookupMapper.toDTO(saved);
    }

    @Override
    public LookupValueDTO update(ObjectId id, LookupValueDTO dto) {
        log.info("Updating lookup value: {}", id);
        LookupValue existing = lookupValueRepository.findById(id)
                .orElseThrow(() -> new LookupException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Lookup value not found",
                        HttpStatus.NOT_FOUND.value()));

        String oldCode = existing.getCode();

        // Partial update: only fields actually provided are overwritten; everything
        // else keeps its current value (so the FE can send just the changed fields).
        if (dto.getType() != null) {
            existing.setType(dto.getType());
        }
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            existing.setCode(dto.getCode());
        }
        if (dto.getLabel() != null && !dto.getLabel().isBlank()) {
            existing.setLabel(dto.getLabel());
        }
        if (dto.getMetadata() != null) {
            existing.setMetadata(dto.getMetadata());
        }
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        }
        if (dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
        }

        LookupValue saved = lookupValueRepository.save(existing);

        // Keep products in sync: a CATEGORY code rename re-points every product that
        // referenced the old code (products store the category as a free-text string).
        String newCode = existing.getCode();
        if (existing.getType() == LookupType.CATEGORY
                && oldCode != null
                && !oldCode.equalsIgnoreCase(newCode)) {
            long moved = mongoTemplate.updateMulti(
                    categoryQuery(oldCode),
                    new Update().set("category", newCode).set("updatedAt", DateUtil.now()),
                    Product.class).getModifiedCount();
            log.info("Category rename '{}' -> '{}' re-pointed {} product(s).", oldCode, newCode, moved);
        }

        return LookupMapper.toDTO(saved);
    }

    /** Matches products whose category equals the given code (case-insensitive, exact). */
    private Query categoryQuery(String code) {
        return new Query(Criteria.where("category").regex("^" + Pattern.quote(code) + "$", "i"));
    }

    @Override
    public void delete(ObjectId id) {
        log.info("Deleting lookup value: {}", id);
        LookupValue value = lookupValueRepository.findById(id)
                .orElseThrow(() -> new LookupException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Lookup value not found",
                        HttpStatus.NOT_FOUND.value()));

        // Don't orphan products: block deleting a category still referenced by products.
        // (To hide it from the dropdown without deleting, set active=false instead.)
        if (value.getType() == LookupType.CATEGORY) {
            long inUse = mongoTemplate.count(categoryQuery(value.getCode()), Product.class);
            if (inUse > 0) {
                throw new LookupException(
                        HttpStatus.CONFLICT.toString(),
                        String.format("Cannot delete category '%s' — %d product(s) still use it. "
                                + "Reassign those products or deactivate the category instead.",
                                value.getCode(), inUse),
                        HttpStatus.CONFLICT.value());
            }
        }

        lookupValueRepository.deleteById(id);
    }
}
