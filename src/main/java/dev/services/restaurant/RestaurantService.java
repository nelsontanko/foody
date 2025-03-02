package dev.services.restaurant;

import dev.account.user.AddressRepository;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.courier.Courier;
import dev.services.courier.CourierMapper;
import dev.services.courier.CourierRepository;
import dev.services.restaurant.RestaurantDTO.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static dev.core.exception.ErrorCode.RESTAURANT_ALREADY_EXISTS;
import static dev.core.exception.ErrorCode.RESTAURANT_NOT_FOUND;

/**
 * @author Nelson Tanko
 */
@Service
public class RestaurantService {
    private static final Logger LOG = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final CourierRepository courierRepository;
    private final AddressRepository addressRepository;
    private final RestaurantMapper restaurantMapper;
    private final CourierMapper courierMapper;

    public RestaurantService(RestaurantRepository restaurantRepository, CourierRepository courierRepository, AddressRepository addressRepository, RestaurantMapper restaurantMapper, CourierMapper courierMapper) {
        this.restaurantRepository = restaurantRepository;
        this.courierRepository = courierRepository;
        this.addressRepository = addressRepository;
        this.restaurantMapper = restaurantMapper;
        this.courierMapper = courierMapper;
    }

    public Response createRestaurant(@Valid Request request) {
        LOG.info("Creating restaurant: {}", request.getName());

        Restaurant restaurant = restaurantMapper.toEntity(request);
        if (addressRepository.existsByLatitudeAndLongitude(restaurant.getAddress().getLatitude(),
                restaurant.getAddress().getLongitude())){
            throw new GenericApiException(RESTAURANT_ALREADY_EXISTS);
        }
        Courier courier = courierMapper.toEntity(request.getCourier());

        restaurant.setCourier(courier);
        courier.setRestaurant(restaurant);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        LOG.info("Restaurant created with ID: {}", savedRestaurant.getId());
        return restaurantMapper.toResponseDto(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public List<Response> getAllRestaurants() {
        LOG.info("Fetching all restaurants");
        List<Restaurant> restaurants = restaurantRepository.findByActiveTrue();
        return restaurantMapper.toResponseDtoList(restaurants);
    }

    @Transactional(readOnly = true)
    public DetailedResponse getRestaurantById(Long id) {
        LOG.info("Fetching restaurant with ID: {}", id);
        Restaurant restaurant = findRestaurantById(id);
        return restaurantMapper.toDetailedResponseDto(restaurant);
    }

    @Transactional
    public Response updateRestaurant(Long id, UpdateRequest request) {
        LOG.info("Updating restaurant with ID: {}", id);
        Restaurant restaurant = findRestaurantById(id);

        restaurantMapper.updateRestaurantFromDto(request, restaurant);

        // Update courier if exists
        if (restaurant.getCourier() != null && request.getCourier() != null) {
            courierMapper.updateCourierFromDto(request.getCourier(), restaurant.getCourier());
        } else if (request.getCourier() != null) {
            // Create a new courier if it doesn't exist
            Courier courier = courierMapper.toEntityUpdate(request.getCourier());
            courier.setRestaurant(restaurant);
            restaurant.setCourier(courier);
        }
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        LOG.info("Restaurant updated: {}", updatedRestaurant.getId());
        return restaurantMapper.toResponseDto(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        LOG.info("Deleting restaurant with ID: {}", id);
        Restaurant restaurant = findRestaurantById(id);

        restaurant.setActive(false);

        if (restaurant.getCourier() != null) {
            restaurant.getCourier().setActive(false);
        }
        restaurantRepository.save(restaurant);
        LOG.info("Restaurant soft deleted: {}", id);
    }

    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::isActive)
                .orElseThrow(() -> new GenericApiException(RESTAURANT_NOT_FOUND));
    }

    @Scheduled(fixedRate = 900000) // Run every 15 minute
    @Transactional
    public void updateAvailabilityStatus() {
        LocalDateTime now = LocalDateTime.now();
        restaurantRepository.updateAvailabilityStatus(now);
        courierRepository.updateAvailabilityStatus(now);
        LOG.debug("Updated availability status for restaurants and couriers");
    }
}
