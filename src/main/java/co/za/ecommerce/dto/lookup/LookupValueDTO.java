package co.za.ecommerce.dto.lookup;

import co.za.ecommerce.model.lookup.LookupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Transport for a {@link co.za.ecommerce.model.lookup.LookupValue}. Doubles as the
 * admin create/update request body — the validation constraints apply when the
 * controller binds it with {@code @Valid}; they are inert on the response path.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LookupValueDTO {

    private String id;

    @NotNull(message = "Lookup type is required")
    private LookupType type;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Label is required")
    private String label;

    private Map<String, Object> metadata;

    /**
     * Nullable so an omitted flag can be distinguished from an explicit {@code false}:
     * on create a null defaults to active ({@code true}); on update a null leaves the
     * existing value unchanged.
     */
    private Boolean active;

    /** Nullable so a partial update can omit it; defaults to 0 on create when omitted. */
    private Integer sortOrder;
}
