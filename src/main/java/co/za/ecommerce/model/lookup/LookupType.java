package co.za.ecommerce.model.lookup;

/**
 * Categories of reference data served by the lookup API. Stored on each
 * {@link LookupValue} document and used to scope lookups by {@code type}.
 */
public enum LookupType {
    PROVINCE,
    DELIVERY_OPTION,
    PAYMENT_METHOD,
    CATEGORY
}
