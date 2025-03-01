package dev;

import dev.core.config.AsyncSyncConfiguration;
import dev.core.config.EmbeddedSQL;
import dev.core.config.JacksonConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.*;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@EmbeddedSQL
public @interface FoodyIntegrationTest {
}
