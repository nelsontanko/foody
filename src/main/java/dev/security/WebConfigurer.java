package dev.security;

import dev.core.config.FoodyProperties;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Set;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * @author Nelson Tanko
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebConfigurer implements ServletContainerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(WebConfigurer.class);

    private final Environment env;

    private final FoodyProperties foodyProperties;

    public WebConfigurer(Environment env, FoodyProperties foodyProperties) {
        this.env = env;
        this.foodyProperties = foodyProperties;
    }

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) {
        if (env.getActiveProfiles().length != 0) {
            LOG.info("Web application configuration, using profiles: {}", (Object[]) env.getActiveProfiles());
        }
        LOG.info("Web application fully configured");
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = foodyProperties.getCors();
        if (!CollectionUtils.isEmpty(config.getAllowedOrigins()) || !CollectionUtils.isEmpty(config.getAllowedOriginPatterns())) {
            LOG.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", config);
            source.registerCorsConfiguration("/management/**", config);
            source.registerCorsConfiguration("/v3/api-docs", config);
            source.registerCorsConfiguration("/swagger-ui/**", config);
        }
        return new CorsFilter(source);
    }
}
