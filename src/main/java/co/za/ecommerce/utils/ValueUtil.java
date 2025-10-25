package co.za.ecommerce.utils;

import java.util.List;

public final class ValueUtil {

    private ValueUtil() {
    }

    /**
     * Returns newValue if it's not null or empty, otherwise returns currentValue.
     *
     * @param newValue     The new string to check
     * @param currentValue The fallback string
     * @return A non-null string
     */
    public static String defaultIfNullOrEmpty(String newValue, String currentValue) {
        return (newValue != null && !newValue.trim().isEmpty()) ? newValue : currentValue;
    }

    /**
     * Returns newValue if it's not null, otherwise returns currentValue.
     * Works for any object type.
     *
     * @param <T>          The type of object
     * @param newValue     The new object to check
     * @param currentValue The fallback object
     * @return A non-null object
     */
    public static <T> T defaultIfNull(T newValue, T currentValue) {
        return (newValue != null) ? newValue : currentValue;
    }

    /**
     * Returns {@code newValue} if it's not null and greater than zero;
     * otherwise returns {@code currentValue}.
     *
     * @param newValue     The new Double to check
     * @param currentValue The fallback Double
     * @return A non-null Double, falling back to {@code currentValue} if {@code newValue} is null or <= 0
     */
    public static Double defaultIfNullOrZero(Double newValue, Double currentValue) {
        return (newValue != null && newValue > 0) ? newValue : currentValue;
    }

    /**
     * Returns {@code newValue} if it's not null and greater than zero;
     * otherwise returns {@code currentValue}.
     *
     * @param newValue     The new Integer to check
     * @param currentValue The fallback Integer
     * @return A non-null Integer, falling back to {@code currentValue} if {@code newValue} is null or <= 0
     */
    public static Integer defaultIfNullOrZero(Integer newValue, Integer currentValue) {
        return (newValue != null && newValue > 0) ? newValue : currentValue;
    }

    /**
     * Returns newValue if it's not null or empty, otherwise returns currentValue.
     * Works for lists of any object type.
     *
     * @param <T>          The type of elements in the list
     * @param newValue     The new list to check
     * @param currentValue The fallback list
     * @return A non-null, possibly empty list
     */
    public static <T> List<T> defaultIfNullOrEmptyList(List<T> newValue, List<T> currentValue) {
        return (newValue != null && !newValue.isEmpty()) ? newValue : currentValue;
    }

}
