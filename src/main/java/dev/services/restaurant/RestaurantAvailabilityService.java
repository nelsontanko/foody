package dev.services.restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
@Service
public class RestaurantAvailabilityService {

    private static final Logger LOG = LoggerFactory.getLogger(RestaurantAvailabilityService.class);
    private static final String RESTAURANT_LOCK_KEY = "restaurant:busy:";
    private static final String ORDER_INFO_KEY = "order:info:";

    @Value("${foody.restaurant.availability.default-busy-minutes:15}")
    private long defaultBusyMinutes;

    private final RedisTemplate<String, String> redisTemplate;

    public RestaurantAvailabilityService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Marks a restaurant as busy in Redis and stores order completion data
     */
    public void markRestaurantAsBusy(Long restaurantId, Long orderId, LocalDateTime deliveryTime) {
        Duration busyDuration = Duration.between(LocalDateTime.now(), deliveryTime);
        long busyMinutes = Math.max(busyDuration.toMinutes(), defaultBusyMinutes);

        String restaurantKey = RESTAURANT_LOCK_KEY + restaurantId;
        String orderInfoKey = ORDER_INFO_KEY + restaurantId;

        redisTemplate.opsForValue().set(restaurantKey, orderId.toString(), Duration.ofMinutes(busyMinutes));
        LOG.info("Restaurant {} marked as busy for {} minutes with order {}", restaurantId, busyMinutes, orderId);

        String orderInfo = String.format("%d:%d", orderId, restaurantId);
        redisTemplate.opsForValue().set(orderInfoKey, orderInfo, Duration.ofMinutes(busyMinutes));
    }

    /**
     * Checks if a restaurant is available (not busy)
     */
    public boolean isRestaurantAvailable(Long restaurantId) {
        String key = RESTAURANT_LOCK_KEY + restaurantId;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Gets the remaining unavailability time in seconds
     */
    public long getRemainingBusyTime(Long restaurantId) {
        String key = RESTAURANT_LOCK_KEY + restaurantId;
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl > 0 ? ttl / 60 : 0; // Convert seconds to minutes
    }

    /**
     * Get the order ID associated with a busy restaurant
     */
    public Long getRestaurantOrderId(Long restaurantId) {
        String key = RESTAURANT_LOCK_KEY + restaurantId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : null;
    }
}
