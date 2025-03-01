package dev;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.lang.annotation.*;

/**
 * @author Nelson Tanko
 * Base annotation for web integration tests
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FoodyIntegrationTest
@AutoConfigureMockMvc
public @interface FoodyWebIntegrationTest {
}
