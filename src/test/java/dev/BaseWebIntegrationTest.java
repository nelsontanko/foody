package dev;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;

/**
 * @author Nelson Tanko
 */
@FoodyWebIntegrationTest
@Sql(value = {"/scripts/authority.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class BaseWebIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    private static final ObjectMapper defaultObjectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setPropertyNamingStrategy(LOWER_CAMEL_CASE);

    protected static String toJSON(final Object object) throws JsonProcessingException {
        return defaultObjectMapper.writeValueAsString(object);
    }
}
