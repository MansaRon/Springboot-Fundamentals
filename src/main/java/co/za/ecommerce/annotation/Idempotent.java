package co.za.ecommerce.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as idempotent.
 * Clients must supply an {@code Idempotency-Key} header (UUID).
 * The first successful (2xx) response is cached in Redis for {@link #ttlHours()} hours;
 * any retry carrying the same key receives the cached response without re-executing the logic.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    long ttlHours() default 24;
}
