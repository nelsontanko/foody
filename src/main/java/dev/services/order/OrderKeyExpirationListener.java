package dev.services.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * @author Nelson Tanko
 */
@Component
public class OrderKeyExpirationListener extends KeyExpirationEventMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderKeyExpirationListener.class);
    private static final String ORDER_INFO_KEY_PREFIX = "order:info:";

    private final OrderCompletionService orderCompletionService;

    @Autowired
    public OrderKeyExpirationListener(RedisMessageListenerContainer listenerContainer, OrderCompletionService orderCompletionService) {
        super(listenerContainer);
        this.orderCompletionService = orderCompletionService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        LOG.debug("Redis key expired: {}", expiredKey);

        if (expiredKey.startsWith(ORDER_INFO_KEY_PREFIX)) {
            try {
                String restaurantIdStr = expiredKey.substring(ORDER_INFO_KEY_PREFIX.length());
                Long restaurantId = Long.parseLong(restaurantIdStr);

                LOG.info("Processing expiration for order info key: {}, restaurant ID: {}",
                        expiredKey, restaurantId);

                orderCompletionService.completeOrderAndFreeRestaurant(restaurantId);
            } catch (Exception e) {
                LOG.error("Error processing expired order key: {}", e.getMessage(), e);
            }
        }
    }
}
