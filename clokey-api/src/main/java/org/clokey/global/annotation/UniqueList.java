package org.clokey.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.clokey.global.validator.UniqueListValidator;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueListValidator.class)
public @interface UniqueList {
    String message() default "중복된 값이 존재합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
