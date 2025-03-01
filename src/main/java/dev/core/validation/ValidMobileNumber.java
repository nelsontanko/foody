package dev.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author Nelson Tanko
 * @see PhoneNumberConstraintValidator
 */
@Constraint(validatedBy = PhoneNumberConstraintValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMobileNumber {

    String message() default "Invalid phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String region() default "NG"; // Default region to use for validation
}