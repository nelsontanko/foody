package dev.services.order;

import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.restaurant.Courier;
import dev.services.restaurant.Restaurant;
import dev.services.restaurant.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nelson Tanko
 */
@Service
@Slf4j
public class OrderCompletionService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderCompletionService(OrderRepository orderRepository, RestaurantRepository restaurantRepository) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void completeOrderAndFreeRestaurant(Long orderId, Long restaurantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GenericApiException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        log.info("Order {} marked as delivered", orderId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new GenericApiException(ErrorCode.RESTAURANT_NOT_FOUND));
        restaurant.markAsAvailable();

        Courier courier = restaurant.getCourier();
        if (courier != null) {
            courier.markAsAvailable();
        }

        restaurantRepository.save(restaurant);
        log.info("Restaurant {} and its courier marked as available", restaurantId);
    }
}
