package dev.core.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

 /**
 * Custom email validation annotation that provides robust email validation
 * without requiring external dependencies.
 */
 @Documented
 @Constraint(validatedBy = EmailConstraintValidator.class)
 @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
 @Retention(RetentionPolicy.RUNTIME)
 public @interface ValidEmail {
     String message() default "Invalid email format";
     Class<?>[] groups() default {};
     Class<? extends Payload>[] payload() default {};
 }