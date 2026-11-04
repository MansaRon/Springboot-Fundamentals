package co.za.ecommerce.model.lookup;

import co.za.ecommerce.model.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Admin-managed reference data (provinces, delivery options, payment methods)
 * served to the frontend via the lookup API so option lists are backend-driven.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "lookup_values")
public class LookupValue extends Entity {

    /** Which lookup list this value belongs to. */
    private LookupType type;

    /** Stable machine value the frontend submits back (e.g. "GAUTENG", "EXPRESS"). */
    private String code;

    /** Human-readable display text (e.g. "Gauteng", "Express Delivery"). */
    private String label;

    /** Free-form extra attributes for the value (e.g. price, estimatedDays). */
    private Map<String, Object> metadata;

    /** Only active values are returned by the public lookup endpoint. */
    private boolean active;

    /** Ascending display order within the type. */
    private int sortOrder;
}
