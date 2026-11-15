package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.exception.LookupException;
import co.za.ecommerce.model.lookup.LookupType;
import co.za.ecommerce.model.lookup.LookupValue;
import co.za.ecommerce.repository.LookupValueRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LookupService Tests")
class LookupServiceImplTest {

    @Mock private LookupValueRepository lookupValueRepository;
    @Mock private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;
    @InjectMocks private LookupServiceImpl lookupService;

    private LookupValue gauteng() {
        return LookupValue.builder()
                .id(new ObjectId())
                .type(LookupType.PROVINCE)
                .code("GAUTENG")
                .label("Gauteng")
                .metadata(Map.of())
                .active(true)
                .sortOrder(3)
                .build();
    }

    @Nested
    @DisplayName("getActiveByType")
    class GetActiveByType {
        @Test
        @DisplayName("shouldReturnMappedActiveValuesForType")
        void shouldReturnMappedActiveValuesForType() {
            when(lookupValueRepository.findByTypeAndActiveTrueOrderBySortOrderAsc(LookupType.PROVINCE))
                    .thenReturn(List.of(gauteng()));

            List<LookupValueDTO> result = lookupService.getActiveByType(LookupType.PROVINCE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("GAUTENG");
            assertThat(result.get(0).getLabel()).isEqualTo("Gauteng");
            assertThat(result.get(0).getType()).isEqualTo(LookupType.PROVINCE);
            verify(lookupValueRepository).findByTypeAndActiveTrueOrderBySortOrderAsc(LookupType.PROVINCE);
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenNoValues")
        void shouldReturnEmptyListWhenNoValues() {
            when(lookupValueRepository.findByTypeAndActiveTrueOrderBySortOrderAsc(LookupType.PAYMENT_METHOD))
                    .thenReturn(List.of());

            assertThat(lookupService.getActiveByType(LookupType.PAYMENT_METHOD)).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("shouldSaveAndReturnCreatedValue")
        void shouldSaveAndReturnCreatedValue() {
            LookupValueDTO dto = LookupValueDTO.builder()
                    .type(LookupType.DELIVERY_OPTION)
                    .code("EXPRESS")
                    .label("Express Delivery")
                    .metadata(Map.of("price", 50.0))
                    .active(true)
                    .sortOrder(2)
                    .build();

            when(lookupValueRepository.save(any(LookupValue.class)))
                    .thenAnswer(invocation -> {
                        LookupValue toSave = invocation.getArgument(0);
                        toSave.setId(new ObjectId());
                        return toSave;
                    });

            LookupValueDTO result = lookupService.create(dto);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getCode()).isEqualTo("EXPRESS");

            ArgumentCaptor<LookupValue> captor = ArgumentCaptor.forClass(LookupValue.class);
            verify(lookupValueRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(LookupType.DELIVERY_OPTION);
            assertThat(captor.getValue().getMetadata()).containsEntry("price", 50.0);
        }

        @Test
        @DisplayName("shouldDefaultActiveToTrueWhenOmitted")
        void shouldDefaultActiveToTrueWhenOmitted() {
            when(lookupValueRepository.save(any(LookupValue.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LookupValueDTO result = lookupService.create(LookupValueDTO.builder()
                    .type(LookupType.CATEGORY).code("APPAREL").label("Apparel").build()); // active omitted

            assertThat(result.getActive()).isTrue();
        }

        @Test
        @DisplayName("shouldRejectDuplicateCodeWithinType")
        void shouldRejectDuplicateCodeWithinType() {
            when(lookupValueRepository.existsByTypeAndCodeIgnoreCase(LookupType.CATEGORY, "APPAREL"))
                    .thenReturn(true);

            assertThatThrownBy(() -> lookupService.create(LookupValueDTO.builder()
                    .type(LookupType.CATEGORY).code("APPAREL").label("Apparel").build()))
                    .isInstanceOf(LookupException.class)
                    .hasMessageContaining("already exists");

            verify(lookupValueRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("shouldUpdateExistingValue")
        void shouldUpdateExistingValue() {
            LookupValue existing = gauteng();
            ObjectId id = existing.getId();

            LookupValueDTO dto = LookupValueDTO.builder()
                    .type(LookupType.PROVINCE)
                    .code("GAUTENG")
                    .label("Gauteng Province")
                    .active(false)
                    .sortOrder(10)
                    .build();

            when(lookupValueRepository.findById(id)).thenReturn(Optional.of(existing));
            when(lookupValueRepository.save(any(LookupValue.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LookupValueDTO result = lookupService.update(id, dto);

            assertThat(result.getLabel()).isEqualTo("Gauteng Province");
            assertThat(result.getActive()).isFalse();
            assertThat(result.getSortOrder()).isEqualTo(10);
        }

        @Test
        @DisplayName("shouldKeepExistingFieldsOnPartialUpdate")
        void shouldKeepExistingFieldsOnPartialUpdate() {
            LookupValue existing = gauteng(); // code GAUTENG, label Gauteng, active true, sortOrder 3
            ObjectId id = existing.getId();
            when(lookupValueRepository.findById(id)).thenReturn(Optional.of(existing));
            when(lookupValueRepository.save(any(LookupValue.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Only toggle active; type/code/label/sortOrder omitted.
            LookupValueDTO result = lookupService.update(id, LookupValueDTO.builder().active(false).build());

            assertThat(result.getCode()).isEqualTo("GAUTENG");
            assertThat(result.getLabel()).isEqualTo("Gauteng");
            assertThat(result.getActive()).isFalse();
            assertThat(result.getSortOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("shouldThrowWhenValueNotFound")
        void shouldThrowWhenValueNotFound() {
            ObjectId id = new ObjectId();
            when(lookupValueRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lookupService.update(id, LookupValueDTO.builder().build()))
                    .isInstanceOf(LookupException.class)
                    .hasMessageContaining("Lookup value not found");

            verify(lookupValueRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldCascadeCategoryCodeRenameToProducts")
        void shouldCascadeCategoryCodeRenameToProducts() {
            LookupValue existing = LookupValue.builder()
                    .id(new ObjectId()).type(LookupType.CATEGORY)
                    .code("APPAREL").label("Apparel").active(true).sortOrder(1).build();
            ObjectId id = existing.getId();
            when(lookupValueRepository.findById(id)).thenReturn(Optional.of(existing));
            when(lookupValueRepository.save(any(LookupValue.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(mongoTemplate.updateMulti(any(), any(), eq(co.za.ecommerce.model.Product.class)))
                    .thenReturn(com.mongodb.client.result.UpdateResult.acknowledged(2, 2L, null));

            lookupService.update(id, LookupValueDTO.builder()
                    .type(LookupType.CATEGORY).code("CLOTHING").label("Apparel").sortOrder(1).build());

            verify(mongoTemplate).updateMulti(any(), any(), eq(co.za.ecommerce.model.Product.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("shouldDeleteWhenValueExists")
        void shouldDeleteWhenValueExists() {
            LookupValue existing = gauteng(); // PROVINCE — no product-usage check
            ObjectId id = existing.getId();
            when(lookupValueRepository.findById(id)).thenReturn(Optional.of(existing));

            lookupService.delete(id);

            verify(lookupValueRepository).deleteById(id);
        }

        @Test
        @DisplayName("shouldThrowWhenValueDoesNotExist")
        void shouldThrowWhenValueDoesNotExist() {
            ObjectId id = new ObjectId();
            when(lookupValueRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lookupService.delete(id))
                    .isInstanceOf(LookupException.class)
                    .hasMessageContaining("Lookup value not found");

            verify(lookupValueRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("shouldBlockDeletingCategoryStillUsedByProducts")
        void shouldBlockDeletingCategoryStillUsedByProducts() {
            LookupValue category = LookupValue.builder()
                    .id(new ObjectId()).type(LookupType.CATEGORY)
                    .code("APPAREL").label("Apparel").active(true).build();
            when(lookupValueRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(mongoTemplate.count(any(), eq(co.za.ecommerce.model.Product.class))).thenReturn(3L);

            assertThatThrownBy(() -> lookupService.delete(category.getId()))
                    .isInstanceOf(LookupException.class)
                    .hasMessageContaining("still use it");

            verify(lookupValueRepository, never()).deleteById(any());
        }
    }
}
