package dev;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

/**
 * @author Nelson Tanko
 * Base annotation for web integration tests
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FoodyIntegrationTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, WithFoodyUserExtension.class})
public @interface FoodyWebIntegrationTest {
}
