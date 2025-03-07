package dev.services.restaurant;

import dev.account.user.AddressMapper;
import dev.account.user.AddressRepository;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.common.CacheService;
import dev.services.restaurant.RestaurantDTO.Request;
import dev.services.restaurant.RestaurantDTO.Response;
import dev.services.restaurant.RestaurantDTO.UpdateRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static dev.core.exception.ErrorCode.RESTAURANT_NOT_FOUND;

/**
 * @author Nelson Tanko
 */
@Service
public class RestaurantService {
    private static final String CACHE_NAME = "restaurants";

    private static final Logger LOG = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final RestaurantMapper restaurantMapper;
    private final CourierMapper courierMapper;
    private final CacheService cacheService;

    public RestaurantService(RestaurantRepository restaurantRepository, AddressRepository addressRepository, RestaurantMapper restaurantMapper, CourierMapper courierMapper, CacheService cacheService) {
        this.restaurantRepository = restaurantRepository;
        this.addressRepository = addressRepository;
        this.restaurantMapper = restaurantMapper;
        this.courierMapper = courierMapper;
        this.cacheService = cacheService;
    }

    @Transactional
    public Response createRestaurant(@Valid Request request) {
        LOG.info("Creating restaurant: {}", request.getName());

        if (isDuplicateAddress(request.getAddress().getLatitude(), request.getAddress().getLongitude())) {
            throw new GenericApiException(ErrorCode.RESTAURANT_ADDRESS_ALREADY_EXISTS);
        }
        Restaurant restaurant = restaurantMapper.toEntity(request);
        restaurantMapper.postProcess(restaurant);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        LOG.info("Restaurant created with ID: {}", savedRestaurant.getId());
        cacheService.evictAllCacheEntries(CACHE_NAME);
        return restaurantMapper.toResponseDto(savedRestaurant);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME)
    public Page<Response> getAllRestaurants(Pageable pageable) {
        LOG.info("Fetching all active restaurants");
        Page<Restaurant> restaurants = restaurantRepository.findByActiveTrueOrderByAvailableDesc(pageable);
        return restaurants.map(restaurantMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME)
    public Response getRestaurantById(Long restaurantId) {
        LOG.info("Fetching restaurant with ID: {}", restaurantId);
        Restaurant restaurant = findRestaurantById(restaurantId);
        return restaurantMapper.toResponseDto(restaurant);
    }

    @Transactional
    public Response updateRestaurant(Long restaurantId, UpdateRequest request) {
        LOG.info("Updating restaurant with ID: {}", restaurantId);
        Restaurant restaurant = findRestaurantById(restaurantId);

        restaurantMapper.updateRestaurantFromDto(request, restaurant);
        updateCourier(request.getCourier(), restaurant);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        cacheService.evictAllCacheEntries(CACHE_NAME);
        LOG.info("Restaurant updated: {}", updatedRestaurant.getId());
        return restaurantMapper.toResponseDto(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        LOG.info("Deleting restaurant with ID: {}", restaurantId);
        Restaurant restaurant = findRestaurantById(restaurantId);

        restaurant.setActive(false);

        if (restaurant.getCourier() != null) {
            restaurant.getCourier().setActive(false);
        }
        restaurantRepository.save(restaurant);
        cacheService.evictAllCacheEntries(CACHE_NAME);
        LOG.info("Restaurant soft deleted: {}", restaurantId);
    }

    private Restaurant findRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .filter(Restaurant::isActive)
                .orElseThrow(() -> new GenericApiException(RESTAURANT_NOT_FOUND));
    }

    private boolean isDuplicateAddress(Double latitude, Double longitude) {
        return addressRepository.existsByLatitudeAndLongitude(latitude, longitude);
    }

    private void updateCourier(CourierDTO.UpdateRequest courierRequest, Restaurant restaurant) {
        Optional.ofNullable(courierRequest).ifPresent(request -> {
            if (restaurant.getCourier() != null) {
                courierMapper.updateCourierFromDto(request, restaurant.getCourier());
            } else {
                Courier newCourier = courierMapper.toEntityUpdate(request);
                newCourier.setRestaurant(restaurant);
                restaurant.setCourier(newCourier);
            }
        });
    }
}
