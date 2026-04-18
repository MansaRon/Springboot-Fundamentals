package co.za.ecommerce.utils;

import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

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

    public static Instant instantNow() {
        return Instant.now();
    }

    public static LocalDate getEstimatedDeliveryDate() {
        int random = new Random().nextInt(20);
        return LocalDate.now().plusDays(random);
    }
}
