package co.za.ecommerce.utils;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public final class DateUtil {
    public static LocalDateTime dateTime(Date date) {
        Objects.requireNonNull(date);
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime dateTime(ObjectId id) {
        Objects.requireNonNull(id);
        return dateTime(id.getDate());
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
