package co.za.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ObjectIdValidator implements ConstraintValidator<ValidObjectId, String> {

    private static final Pattern HEX_24 = Pattern.compile("^[a-fA-F0-9]{24}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        return HEX_24.matcher(value).matches();
    }
}
