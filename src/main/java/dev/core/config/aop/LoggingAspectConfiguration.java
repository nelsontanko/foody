package dev.core.config.aop;

import dev.core.config.FoodyConstants;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

/**
 * @author Nelson Tanko
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile(FoodyConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}