package co.za.ecommerce.utils;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("DateUtilTest Tests")
class DateUtilTest {
    @Test
    @DisplayName("shouldConvertDateToLocalDateTimeCorrectly")
    void shouldConvertDateToLocalDateTimeCorrectly() {
        Date date = new Date(0);

        LocalDateTime result = DateUtil.dateTime(date);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("shouldThrowNullPointerExceptionWhenDateIsNull")
    void shouldThrowNullPointerExceptionWhenDateIsNull() {
        assertThatThrownBy(() -> DateUtil.dateTime((Date) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("shouldExtractTimestampFromObjectId")
    void shouldExtractTimestampFromObjectId() {
        ObjectId id = new ObjectId();

        LocalDateTime result = DateUtil.dateTime(id);

        assertThat(result).isNotNull();
        assertThat(result).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(result).isAfter(LocalDateTime.now().minusSeconds(5));
    }

    @Test
    @DisplayName("shouldThrowNullPointerExceptionWhenObjectIdIsNull")
    void shouldThrowNullPointerExceptionWhenObjectIdIsNull() {
        assertThatThrownBy(() -> DateUtil.dateTime((ObjectId) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("shouldReturnNonNullLocalDateTimeForNow")
    void shouldReturnNonNullLocalDateTimeForNow() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = DateUtil.now();
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isNotNull();
        assertThat(result).isAfterOrEqualTo(before);
        assertThat(result).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("shouldReturnNonNullInstantForInstantNow")
    void shouldReturnNonNullInstantForInstantNow() {
        Instant before = Instant.now();
        Instant result = DateUtil.instantNow();
        Instant after = Instant.now();

        assertThat(result).isNotNull();
        assertThat(result).isAfterOrEqualTo(before);
        assertThat(result).isBeforeOrEqualTo(after);
    }
}