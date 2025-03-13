package dev.core.config.redis;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nelson Tanko
 */
public class PrefixRoutingCacheManager implements CacheManager {

    private static final String CAFFEINE_PREFIX = "caffeine_";
    private static final String REDIS_PREFIX = "redis_";

    private final CaffeineCacheManager caffeineCacheManager;
    private final RedisCacheManager redisCacheManager;

    public PrefixRoutingCacheManager(CaffeineCacheManager caffeineCacheManager, RedisCacheManager redisCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
    }

    @Override
    public org.springframework.cache.Cache getCache(String name) {
        if (name.startsWith(CAFFEINE_PREFIX)) {
            String caffeineName = name.substring(CAFFEINE_PREFIX.length());
            return caffeineCacheManager.getCache(caffeineName);
        } else if (name.startsWith(REDIS_PREFIX)) {
            String redisName = name.substring(REDIS_PREFIX.length());
            return redisCacheManager.getCache(redisName);
        } else {
            return redisCacheManager.getCache(name);
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> cacheNames = new HashSet<>();

        caffeineCacheManager.getCacheNames().forEach(name -> cacheNames.add(CAFFEINE_PREFIX + name));
        redisCacheManager.getCacheNames().forEach(name -> cacheNames.add(REDIS_PREFIX + name));

        return cacheNames;
    }
}

