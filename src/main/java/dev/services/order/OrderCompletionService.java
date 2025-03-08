package dev.services.order;

import ch.qos.logback.core.spi.ConfigurationEvent;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.restaurant.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Nelson Tanko
 */
@Service
public class OrderCompletionService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCompletionService.class);
    private static final String RESTAURANT_LOCK_KEY = "restaurant:busy:";

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final CourierRepository courierRepository;

    public OrderCompletionService(OrderRepository orderRepository, RestaurantRepository restaurantRepository, RedisTemplate<String, String> redisTemplate,
                                  CourierRepository courierRepository) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.redisTemplate = redisTemplate;
        this.courierRepository = courierRepository;
    }

    @Transactional
    public void completeOrderAndFreeRestaurant(Long restaurantId) {
        LOG.info("Processing completion for restaurant {}", restaurantId);

        try {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new GenericApiException(ErrorCode.RESTAURANT_NOT_FOUND));
            restaurant.markAsAvailable();
            restaurantRepository.save(restaurant);

            Courier courier = restaurant.getCourier();
            if (courier != null) {
                courier.markAsAvailable();
                courierRepository.save(courier);
            }
            orderRepository.findByRestaurantIdAndStatusOrderByCreatedDateDesc(
                            restaurantId, OrderStatus.DELIVERING)
                    .stream()
                    .findFirst()
                    .ifPresent(order -> {
                        order.setStatus(OrderStatus.DELIVERED);
                        orderRepository.save(order);
                        LOG.info("Order {} marked as delivered", order.getId());
                    });

            redisTemplate.delete(RESTAURANT_LOCK_KEY + restaurant.getId());
            LOG.info("Restaurant {} and its courier marked as available", restaurantId);
        }catch (Exception ex){
            LOG.error("Error completing order for restaurant {}: {}", restaurantId, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Initialize Redis config at startup to support keyspace events
     */
    @PostConstruct
    public void init() {
        try {
            RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection();
            ConfigurationEvent.newConfigurationChangeDetectedEvent(connection.serverCommands());
            LOG.info("Redis keyspace notifications configured successfully");
        } catch (Exception e) {
            LOG.warn("Failed to configure Redis keyspace notifications: {}", e.getMessage(), e);
            LOG.warn("You may need to manually run: CONFIG SET notify-keyspace-events Ex");
        }
    }
}
