package org.clokey.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import org.clokey.global.annotation.UniqueList;

public class UniqueListValidator implements ConstraintValidator<UniqueList, List<?>> {
    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.size() == new HashSet<>(value).size();
    }
}
