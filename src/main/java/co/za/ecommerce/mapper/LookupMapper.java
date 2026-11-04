package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.model.lookup.LookupValue;

import java.util.List;
import java.util.stream.Collectors;

public class LookupMapper {

    private LookupMapper() {
    }

    public static LookupValueDTO toDTO(LookupValue value) {
        if (value == null) return null;
        return LookupValueDTO.builder()
                .id(value.getId() != null ? value.getId().toHexString() : null)
                .type(value.getType())
                .code(value.getCode())
                .label(value.getLabel())
                .metadata(value.getMetadata())
                .active(value.isActive())
                .sortOrder(value.getSortOrder())
                .build();
    }

    public static List<LookupValueDTO> toDTOList(List<LookupValue> values) {
        if (values == null) return null;
        return values.stream().map(LookupMapper::toDTO).collect(Collectors.toList());
    }

    /** Builds a new entity from a create request (id is assigned on save). */
    public static LookupValue toEntity(LookupValueDTO dto) {
        if (dto == null) return null;
        return LookupValue.builder()
                .type(dto.getType())
                .code(dto.getCode())
                .label(dto.getLabel())
                .metadata(dto.getMetadata())
                .active(dto.isActive())
                .sortOrder(dto.getSortOrder())
                .build();
    }
}
