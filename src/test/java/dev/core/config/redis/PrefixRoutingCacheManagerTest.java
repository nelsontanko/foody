package dev.core.config.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrefixRoutingCacheManagerTest {

    @Mock private CaffeineCacheManager caffeineCacheManager;

    @Mock private RedisCacheManager redisCacheManager;

    @Mock private Cache caffeineCache;

    @Mock private Cache redisCache;

    @InjectMocks private PrefixRoutingCacheManager cacheManager;

    @Test
    void testGetCaffeineCache() {
        // When
        when(caffeineCacheManager.getCache("testData")).thenReturn(caffeineCache);
        Cache cache = cacheManager.getCache("caffeine_testData");

        // Then
        assertSame(caffeineCache, cache);
        verify(caffeineCacheManager).getCache("testData");
        verify(redisCacheManager, never()).getCache(anyString());
    }

    @Test
    void testGetRedisCache() {
        // When
        when(redisCacheManager.getCache("testData")).thenReturn(redisCache);
        Cache cache = cacheManager.getCache("redis_testData");

        // Then
        assertSame(redisCache, cache);
        verify(redisCacheManager).getCache("testData");
        verify(caffeineCacheManager, never()).getCache(anyString());
    }

    @Test
    void testGetDefaultCache() {
        // When
        when(redisCacheManager.getCache("testData")).thenReturn(redisCache);
        Cache cache = cacheManager.getCache("testData");

        // Then
        assertSame(redisCache, cache);
        verify(redisCacheManager).getCache("testData");
    }

    @Test
    void testGetCacheNames() {
        // When
        when(caffeineCacheManager.getCacheNames()).thenReturn(Set.of("testData", "userData"));
        when(redisCacheManager.getCacheNames()).thenReturn(Set.of("testData", "productData"));
        Collection<String> cacheNames = cacheManager.getCacheNames();

        // Then
        assertEquals(4, cacheNames.size());
        assertTrue(cacheNames.contains("caffeine_testData"));
        assertTrue(cacheNames.contains("caffeine_userData"));
        assertTrue(cacheNames.contains("redis_testData"));
        assertTrue(cacheNames.contains("redis_productData"));
    }
}