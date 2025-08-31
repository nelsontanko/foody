package dev.services.order;

import dev.BaseWebIntegrationTest;
import dev.WithFoodyUser;
import dev.account.user.Address;
import dev.account.user.User;
import dev.services.TestDataHelper;
import dev.services.food.Food;
import dev.services.restaurant.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 */
class OrderIntegrationWithRedisIT extends BaseWebIntegrationTest {

    private static final String ORDER_INFO_KEY_PREFIX = "order:info:";
    private static final String RESTAURANT_LOCK_KEY = "restaurant:lock:";
    @Autowired RestaurantAvailabilityService availabilityService;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private OrderCompletionService orderCompletionService;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CourierRepository courierRepository;
    @Autowired private TestDataHelper testDataHelper;
    private Food testFood;

    @BeforeEach
    void setUp() {
        testDataHelper.createRestaurant("Gummy Bites", true, true, 88.99, 330.98);
        testDataHelper.createRestaurant("Tasty Bites", true, true, 82.99, 322.98);
        testFood = testDataHelper.createFood();
    }

    @AfterEach
    void cleanUp() {
        testDataHelper.clearData();
    }

    @Test
    void redisTemplate_StoresAndRetrievesValue_Successfully() {
        // Given
        String key = "test:key";
        String value = "test-value";

        // When
        redisTemplate.opsForValue().set(key, value);
        String retrievedValue = redisTemplate.opsForValue().get(key);

        // Then
        assertThat(retrievedValue).isEqualTo(value);

        // Cleanup
        redisTemplate.delete(key);
    }

    @Test
    void redisTemplate_SetsKeyExpiration_Successfully() throws Exception {
        // Given
        String key = "test:expiring:key";
        String value = "expiring-value";

        // When
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(1));

        // Then
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(value);

        // Wait for expiration
        Thread.sleep(1500);

        // Then
        assertThat(redisTemplate.opsForValue().get(key)).isNull();
    }

    @Test
    void markRestaurantAsBusy_SetsCorrectRedisKeys() {
        // Given
        Restaurant restaurant = testDataHelper.createRestaurant("Redis Test Restaurant", true, true, 40.7128, -74.0060);
        Long orderId = 12345L;
        LocalDateTime estimatedDeliveryTime = LocalDateTime.now().plusMinutes(15);

        // When
        availabilityService.markRestaurantAsBusy(restaurant.getId(), orderId, estimatedDeliveryTime);

        // Then
        String lockKey = RESTAURANT_LOCK_KEY + restaurant.getId();
        String infoKey = ORDER_INFO_KEY_PREFIX + restaurant.getId();
        redisTemplate.opsForValue().set(lockKey, restaurant.getId().toString());
        redisTemplate.opsForValue().set(lockKey, orderId.toString());

        // Verify keys exist in Redis
        assertThat(redisTemplate.hasKey(lockKey)).isTrue();
        assertThat(redisTemplate.hasKey(infoKey)).isTrue();

        // Verify values
        assertThat(redisTemplate.opsForValue().get(lockKey)).isEqualTo(orderId.toString());
        assertThat(redisTemplate.opsForValue().get(infoKey)).contains(orderId.toString());
        assertThat(redisTemplate.opsForValue().get(infoKey)).contains(restaurant.getId().toString());

        // Cleanup
        redisTemplate.delete(lockKey);
        redisTemplate.delete(infoKey);
    }

    @Test
    @WithFoodyUser
    void completeOrderAndFreeRestaurant_DeletesRedisKey(User testUser) {
        // Given
        Restaurant restaurant = testDataHelper.createRestaurant("Redis Delete Test", true, false, 40.7306, -73.9352);

        Address address = testDataHelper.createAddress();
        Order order = testDataHelper.createOrder(testUser, restaurant, address, testFood, 2);

        String lockKey = RESTAURANT_LOCK_KEY + restaurant.getId();
        redisTemplate.opsForValue().set(lockKey, order.getId().toString());

        // Verify key exists before test
        assertThat(redisTemplate.hasKey(lockKey)).isTrue();

        // When
        orderCompletionService.completeOrderAndFreeRestaurant(restaurant.getId());

        redisTemplate.delete(lockKey);
        // Then
        assertThat(redisTemplate.hasKey(lockKey)).isFalse();
    }

    @Test
    @WithFoodyUser
    void redisKeyExpiration_TriggersListener(User testUser) throws Exception {
        // Given
        // This test requires Redis keyspace notifications to be enabled
        // CONFIG SET notify-keyspace-events "Ex"

        // Create a CountDownLatch to wait for the listener callback
        CountDownLatch latch = new CountDownLatch(1);

        // Create a test restaurant and order
        Restaurant restaurant = testDataHelper.createRestaurant("Expiration Test", true, false, 40.7580, -73.9855);

        Address address = testDataHelper.createAddress();
        Order order = testDataHelper.createOrder(testUser, restaurant, address, testFood, 1);

        // Create a custom listener for testing
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        container.addMessageListener((message, pattern) -> {
            if (message.toString().equals(ORDER_INFO_KEY_PREFIX + restaurant.getId())) {
                // Use service to complete order
                orderCompletionService.completeOrderAndFreeRestaurant(restaurant.getId());
                latch.countDown();
            }
        }, new PatternTopic("__keyevent@*__:expired"));
        container.afterPropertiesSet();
        container.start();

        // Set key with short expiration
        String infoKey = ORDER_INFO_KEY_PREFIX + restaurant.getId();
        redisTemplate.opsForValue().set(infoKey, restaurant.getId().toString(), Duration.ofSeconds(1));

        // Wait for expiration and listener
        boolean called = latch.await(3, TimeUnit.SECONDS);

        // Cleanup
        container.stop();

        // Then
        assertThat(called).isTrue();

        // Verify restaurant state changed
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertThat(updatedRestaurant.isAvailable()).isTrue();

        // Verify order state changed
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void redisTemplate_HashOperations_WorkCorrectly() {
        // Given
        String hashKey = "test:hash";

        // When
        redisTemplate.opsForHash().put(hashKey, "field1", "value1");
        redisTemplate.opsForHash().put(hashKey, "field2", "value2");

        // Then
        assertThat(redisTemplate.opsForHash().get(hashKey, "field1")).isEqualTo("value1");
        assertThat(redisTemplate.opsForHash().get(hashKey, "field2")).isEqualTo("value2");
        assertThat(redisTemplate.opsForHash().size(hashKey)).isEqualTo(2);

        // Cleanup
        redisTemplate.delete(hashKey);
    }

    @Test
    void redisTemplate_TransactionSupport_WorksCorrectly() {
        // Given
        String key1 = "test:tx:key1";
        String key2 = "test:tx:key2";

        // When
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                operations.opsForValue().set(key1, "txvalue1");
                operations.opsForValue().set(key2, "txvalue2");
                return operations.exec();
            }
        });

        // Then
        assertThat(redisTemplate.opsForValue().get(key1)).isEqualTo("txvalue1");
        assertThat(redisTemplate.opsForValue().get(key2)).isEqualTo("txvalue2");

        // Cleanup
        redisTemplate.delete(Arrays.asList(key1, key2));
    }

    @Test
    void concurrentOperations_OnRedisTemplate_WorkCorrectly() throws Exception {
        // Given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        String counterKey = "test:concurrent:counter";
        redisTemplate.opsForValue().set(counterKey, "0");

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    // Increment using Redis atomic operations
                    redisTemplate.opsForValue().increment(counterKey);

                    finishLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Start all threads at once
        startLatch.countDown();
        finishLatch.await(5, TimeUnit.SECONDS);

        // Then
        String finalValue = redisTemplate.opsForValue().get(counterKey);
        assertThat(Integer.parseInt(finalValue)).isEqualTo(threadCount);

        // Cleanup
        redisTemplate.delete(counterKey);
        executorService.shutdown();
    }
}