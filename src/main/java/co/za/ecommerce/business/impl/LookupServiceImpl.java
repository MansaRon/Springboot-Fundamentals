package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.LookupService;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.exception.LookupException;
import co.za.ecommerce.mapper.LookupMapper;
import co.za.ecommerce.model.lookup.LookupType;
import co.za.ecommerce.model.lookup.LookupValue;
import co.za.ecommerce.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LookupServiceImpl implements LookupService {

    private final LookupValueRepository lookupValueRepository;

    @Override
    public List<LookupValueDTO> getActiveByType(LookupType type) {
        log.info("Fetching active lookup values for type: {}", type);
        return LookupMapper.toDTOList(
                lookupValueRepository.findByTypeAndActiveTrueOrderBySortOrderAsc(type));
    }

    @Override
    public List<LookupValueDTO> getAllByType(LookupType type) {
        log.info("Fetching all lookup values (admin) for type: {}", type);
        return LookupMapper.toDTOList(
                lookupValueRepository.findByTypeOrderBySortOrderAsc(type));
    }

    @Override
    public LookupValueDTO create(LookupValueDTO dto) {
        log.info("Creating lookup value: type={}, code={}", dto.getType(), dto.getCode());
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

        existing.setType(dto.getType());
        existing.setCode(dto.getCode());
        existing.setLabel(dto.getLabel());
        existing.setMetadata(dto.getMetadata());
        existing.setActive(dto.isActive());
        existing.setSortOrder(dto.getSortOrder());

        return LookupMapper.toDTO(lookupValueRepository.save(existing));
    }

    @Override
    public void delete(ObjectId id) {
        log.info("Deleting lookup value: {}", id);
        if (!lookupValueRepository.existsById(id)) {
            throw new LookupException(
                    HttpStatus.NOT_FOUND.toString(),
                    "Lookup value not found",
                    HttpStatus.NOT_FOUND.value());
        }
        lookupValueRepository.deleteById(id);
    }
}
