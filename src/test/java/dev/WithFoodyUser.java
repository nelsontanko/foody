package dev;

import java.lang.annotation.*;

/**
 * Runs the annotated method with the given user logged in.
 * @author Nelson Tanko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WithFoodyUser {
    String email() default "default@example.com";
    String[] authorities() default {"ROLE_USER"};
    boolean authenticated() default true;
}
