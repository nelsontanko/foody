package dev.core.config.redis;

import dev.services.common.RateLimit;
import io.github.bucket4j.*;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nelson Tanko
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final LettuceBasedProxyManager proxyManager;
    private final Map<String, BucketConfiguration> configCache = new ConcurrentHashMap<>();


    @Value("${api.rate-limit.header:X-API-Key}")
    private String apiKeyHeader;

    @Value("${api.rate-limit.enabled:true}")
    private boolean rateLimitingEnabled;

    public RateLimitInterceptor(LettuceBasedProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitingEnabled || !(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return true;
        }

        String key = resolveClientKey(request, rateLimit);

        Bucket bucket = resolveBucket(key, rateLimit);

        if (bucket.tryConsume(1)) {
            response.setHeader("X-Rate-Limit", String.valueOf(rateLimit.limit()));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            response.setHeader("X-Rate-Limit-Retry-After",
                    String.valueOf(Duration.ofSeconds(rateLimit.duration()).toSeconds()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }
    }

    private String resolveClientKey(HttpServletRequest request, RateLimit rateLimit) {
        String endpoint = request.getRequestURI();
        return switch (rateLimit.strategy()) {
            case "user" -> {
                String userId = getUserId();
                yield endpoint + ".user." + (userId != null ? userId : "anonymous");
            }
            case "ip" -> endpoint + ".ip." + getClientIp(request);
            case "api" -> {
                String apiKey = request.getHeader(apiKeyHeader);
                yield endpoint + ":" + (apiKey != null ? apiKey : "anonymous");
            }
            default -> endpoint;
        };
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                return ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                return authentication.getName();
            }
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket resolveBucket(String key, RateLimit rateLimitAnnotation) {
        BucketConfiguration configuration = configCache.computeIfAbsent(
                key, k -> {
                    long limit = rateLimitAnnotation.limit();
                    Refill refill = Refill.intervally(limit, Duration.of(rateLimitAnnotation.duration(), ChronoUnit.SECONDS));
                    Bandwidth bandwidth = Bandwidth.classic(limit, refill);
                    return BucketConfiguration.builder().addLimit(bandwidth).build();
                });

        return proxyManager.builder().build(key.getBytes(), () -> configuration);
    }
}
