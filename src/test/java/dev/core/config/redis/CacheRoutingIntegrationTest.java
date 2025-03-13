package dev.core.config.redis;

import dev.BaseWebIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CacheRoutingIntegrationTest extends BaseWebIntegrationTest {

    @Autowired
    private TestCacheService testCacheService;

    @SpyBean
    private TestCacheService testCacheServiceSpy;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            if (cacheManager.getCache(name) != null) {
                cacheManager.getCache(name).clear();
            }
        });
    }

    @Test
    void testCaffeineCache() {
        // First call should execute the method
        String result1 = testCacheService.getCaffeineCachedData("key1");

        // Second call with same key should return cached result
        String result2 = testCacheService.getCaffeineCachedData("key1");

        // Verify the method was only called once
        verify(testCacheServiceSpy, times(1)).getCaffeineCachedData("key1");

        // Results should be the same
        assertEquals(result1, result2);

        // Different key should execute the method again
        testCacheService.getCaffeineCachedData("key2");
        verify(testCacheServiceSpy, times(1)).getCaffeineCachedData("key2");
    }

    @Test
    void testRedisCache() {
        String result1 = testCacheService.getRedisCachedData("key1");

        String result2 = testCacheService.getRedisCachedData("key1");

        verify(testCacheServiceSpy, times(1)).getRedisCachedData("key1");

        assertEquals(result1, result2);

        testCacheService.getRedisCachedData("key2");
        verify(testCacheServiceSpy, times(1)).getRedisCachedData("key2");
    }

    @Test
    void testDefaultCache() {
        String result1 = testCacheService.getDefaultCachedData("key1");

        String result2 = testCacheService.getDefaultCachedData("key1");

        verify(testCacheServiceSpy, times(1)).getDefaultCachedData("key1");

        assertEquals(result1, result2);
    }

    @Test
    void testCacheEviction() {
        testCacheService.getCaffeineCachedData("evictKey");

        testCacheService.evictCaffeineCache("evictKey");

        testCacheService.getCaffeineCachedData("evictKey");

        verify(testCacheServiceSpy, times(2)).getCaffeineCachedData("evictKey");
    }

    @Test
    void testCorrectCacheManagerUsed() {
        assertNotNull(cacheManager.getCache("caffeine_test"));
        assertNotNull(cacheManager.getCache("redis_test"));

    }

    @Service
    public static class TestCacheService {

        @Cacheable(value = "caffeine_testData")
        public String getCaffeineCachedData(String key) {
            // Simulate a slow method call that should be cached
            return "caffeine-data-" + key + "-" + UUID.randomUUID();
        }

        @Cacheable(value = "redis_testData")
        public String getRedisCachedData(String key) {
            return "redis-data-" + key + "-" + UUID.randomUUID();
        }

        @Cacheable(value = "defaultData")
        public String getDefaultCachedData(String key) {
            return "default-data-" + key + "-" + UUID.randomUUID();
        }

        @org.springframework.cache.annotation.CacheEvict(value = "caffeine_testData")
        public void evictCaffeineCache(String key) {
            // Method to evict the cache
        }
    }
}