package co.za.ecommerce.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValueUtil - defaultIfNullOrEmpty")
class ValueUtilTest {
    @Test
    @DisplayName("shouldReturnNewValueWhenNewValueIsValid")
    void shouldReturnNewValueWhenNewValueIsValid() {
        assertThat(ValueUtil.defaultIfNullOrEmpty("new title", "old title")).isEqualTo("new title");
    }

    @Test
    @DisplayName("shouldReturnCurrentValueWhenNewValueIsNull")
    void shouldReturnCurrentValueWhenNewValueIsNull() {
        assertThat(ValueUtil.defaultIfNullOrEmpty(null, "old title")).isEqualTo("old title");
    }

    @Test
    @DisplayName("shouldReturnCurrentValueWhenNewValueIsEmpty")
    void shouldReturnCurrentValueWhenNewValueIsEmpty() {
        assertThat(ValueUtil.defaultIfNullOrEmpty("", "old title")).isEqualTo("old title");
    }

    @Test
    @DisplayName("shouldWorkWithNonStringTypes")
    void shouldWorkWithNonStringTypes() {
        assertThat(ValueUtil.defaultIfNull(42, 0)).isEqualTo(42);
        assertThat(ValueUtil.defaultIfNull(null, 99)).isEqualTo(99);
    }

    @Test
    @DisplayName("shouldReturnNewValueWhenNewValueIsPositive")
    void shouldReturnNewValueWhenNewValueIsPositive() {
        assertThat(ValueUtil.defaultIfNullOrZero(49.99, 0.0)).isEqualTo(49.99);
    }

    @Test
    @DisplayName("shouldReturnCurrentValueWhenNewValueIsZero")
    void shouldReturnCurrentValueWhenNewValueIsZero() {
        assertThat(ValueUtil.defaultIfNullOrZero(0.0, 49.99)).isEqualTo(49.99);
    }

    @Test
    @DisplayName("shouldReturnCurrentValueWhenNewValueIsNegative")
    void shouldReturnCurrentValueWhenNewValueIsNegative() {
        assertThat(ValueUtil.defaultIfNullOrZero(-10.0, 49.99)).isEqualTo(49.99);
    }


    @Test
    @DisplayName("shouldReturnNewListWhenNewListIsPopulated")
    void shouldReturnNewListWhenNewListIsPopulated() {
        List<String> newList = List.of("a", "b");
        List<String> currentList = List.of("x", "y", "z");

        assertThat(ValueUtil.defaultIfNullOrEmptyList(newList, currentList)).isEqualTo(newList);
    }

    @Test
    @DisplayName("shouldReturnCurrentListWhenNewListIsNull")
    void shouldReturnCurrentListWhenNewListIsNull() {
        List<String> currentList = List.of("x", "y");

        assertThat(ValueUtil.defaultIfNullOrEmptyList(null, currentList)).isEqualTo(currentList);
    }

}