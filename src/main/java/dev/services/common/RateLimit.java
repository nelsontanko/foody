package dev.services.common;

import java.lang.annotation.*;

/**
 * @author Nelson Tanko
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Number of API calls allowed within the specified time period.
     */
    long limit() default 20;

    /**
     * Time window for the rate limit in seconds.
     */
    long duration() default 60;

    /**
     * Rate limiting strategy: "user" (by authenticated user), "ip" (by IP address), or "api" (global for endpoint)
     */
    String strategy() default "user";
}
