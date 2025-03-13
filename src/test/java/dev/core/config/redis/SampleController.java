package dev.core.config.redis;


import dev.services.common.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SampleController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint with no rate limiting";
    }

    @RateLimit(limit = 5, duration = 60, strategy = "ip")
    @GetMapping("/limited")
    public String limitedEndpoint() {
        return "This endpoint is limited to 5 requests per minute per IP";
    }

    @RateLimit(limit = 10, duration = 60, strategy = "api")
    @GetMapping("/api-limited")
    public String apiLimitedEndpoint() {
        return "This endpoint is limited to 10 requests per minute per API key";
    }

    @RateLimit(limit = 20, duration = 60, strategy = "user")
    @GetMapping("/user-limited")
    public String userLimitedEndpoint() {
        return "This endpoint is limited to 20 requests per minute per user";
    }
}