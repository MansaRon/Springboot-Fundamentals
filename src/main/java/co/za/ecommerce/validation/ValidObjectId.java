package co.za.ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ObjectIdValidator.class)
public @interface ValidObjectId {
    String message() default "Invalid ID format. Must be a 24-character hexadecimal string";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
