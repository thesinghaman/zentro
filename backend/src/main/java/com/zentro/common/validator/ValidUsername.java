package com.zentro.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for usernames
 * Prevents offensive, reserved, and system usernames
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
@Documented
public @interface ValidUsername {

    String message() default "Username contains inappropriate or reserved words";

    /**
     * These two are required by Jakarta Validation spec so that your custom
     * annotation works with Validator.
     * Even if you don’t use them, you must declare them.
     */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
