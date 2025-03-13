package dev.core.config.redis;

import dev.BaseWebIntegrationTest;
import dev.WithFoodyUser;
import dev.services.TestDataHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@Disabled("Flaky")
class RateLimitIntegrationTest extends BaseWebIntegrationTest {

    @Autowired protected TestDataHelper testDataHelper;

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4"))
            .withExposedPorts(6379);

    @AfterEach
    void cleanUp(){
        testDataHelper.clearData();
    }

    @Test
    @WithFoodyUser
    void testPublicEndpoint_NoRateLimiting() throws Exception {
        mockMvc.perform(get("/api/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a public endpoint with no rate limiting"));
    }


    @Test
    @WithFoodyUser
    void testLimitedEndpoint_RateLimitingInAction() throws Exception {
        // First 5 requests should succeed
        for (int i = 0; i < 5; i++) {
            ResultActions result = mockMvc.perform(get("/api/limited"));
            result.andExpect(status().isOk())
                    .andExpect(header().exists("X-Rate-Limit-Limit"))
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
        }

        // The 6th request should be rate limited
        mockMvc.perform(get("/api/limited"))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
                .andExpect(header().exists("X-Rate-Limit-Retry-After"))
                .andExpect(content().string("Rate limit exceeded. Try again later."));
    }

    @Test
    @WithFoodyUser
    void testApiKeyBasedRateLimiting() throws Exception {
        // Use two different API keys
        String apiKey1 = "test-api-key-1";
        String apiKey2 = "test-api-key-2";

        // First API key can make 10 requests
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/api-limited").header("X-API-Key", apiKey1))
                    .andExpect(status().isOk());
        }

        // 11th request with first API key should be rate limited
        mockMvc.perform(get("/api/api-limited").header("X-API-Key", apiKey1))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()));

        // Second API key should still be able to make requests
        mockMvc.perform(get("/api/api-limited").header("X-API-Key", apiKey2))
                .andExpect(status().isOk());
    }

    @Test
    @WithFoodyUser
    void testIpBasedRateLimiting_DifferentIps() throws Exception {
        // First request with one IP address
        mockMvc.perform(get("/api/limited")
                        .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk());

        // Request with a different IP address should succeed even if the first IP is rate limited
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/limited")
                            .header("X-Forwarded-For", "192.168.1.2"))
                    .andExpect(status().isOk());
        }

        // Next request with the second IP should be rate limited
        mockMvc.perform(get("/api/limited")
                        .header("X-Forwarded-For", "192.168.1.2"))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()));

        // But the first IP can still make more requests (up to its limit)
        mockMvc.perform(get("/api/limited")
                        .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk());
    }
}