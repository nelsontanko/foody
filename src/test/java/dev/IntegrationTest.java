package dev;

import dev.config.AsyncSyncConfiguration;
import dev.config.EmbeddedSQL;
import dev.config.JacksonConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.*;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { FoodyApplication.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
public @interface IntegrationTest {
}
