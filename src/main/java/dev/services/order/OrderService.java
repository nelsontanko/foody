package dev.services.order;

import dev.account.user.*;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.food.Food;
import dev.services.food.FoodRepository;
import dev.services.order.OrderDTO.Request;
import dev.services.order.OrderDTO.Response;
import dev.services.restaurant.Restaurant;
import dev.services.restaurant.RestaurantAvailabilityService;
import dev.services.restaurant.RestaurantRepository;
import dev.services.util.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Nelson Tanko
 */
@Service
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;
    private final RestaurantAvailabilityService restaurantAvailabilityService;
    private final AuthenticatedUser auth;

    public OrderService(OrderRepository orderRepository, FoodRepository foodRepository, RestaurantRepository restaurantRepository,
                        AddressRepository addressRepository, OrderMapper orderMapper, RestaurantAvailabilityService restaurantAvailabilityService, AuthenticatedUser auth) {
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
        this.addressRepository = addressRepository;
        this.orderMapper = orderMapper;
        this.restaurantAvailabilityService = restaurantAvailabilityService;
        this.auth = auth;
    }

    @Transactional
    public Response createOrder(Request request) {
        User user = auth.getAuthenticatedUser();

        Address deliveryAddress = resolveDeliveryAddress(user, request.getDeliveryAddress());
        Restaurant restaurant = findAvailableRestaurant(deliveryAddress);

        List<OrderItem> orderItems = buildOrderItems(request.getOrderItems());
        BigDecimal totalAmount = calculateTotalAmount(orderItems);

        Order order = createNewOrder(user, restaurant, deliveryAddress, orderItems, totalAmount);

        markRestaurantAndCourierAsBusy(restaurant, order);

        LOG.info("Order created successfully with id: {}", order.getId());
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public Page<Response> getUserOrders(Pageable pageable){
        User user = auth.getAuthenticatedUser();
        Page<Order> orders = orderRepository.findAllByUserOrderByOrderTimeDesc(user, pageable);

        return orders.map(orderMapper::toDto);
    }

    public Response updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new GenericApiException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus().equals(OrderStatus.DELIVERED)){
            throw new GenericApiException(ErrorCode.ORDER_ALREADY_DELIVERED);
        }
        order.setStatus(newStatus);
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    private Address resolveDeliveryAddress(User user, AddressDTO.Request address) {
        if (address != null){
            return findOrCreateAddress(user, address);
        }
        // Even when no address is provided, the last used one will suffice
        List<Address> userAddresses = addressRepository.findByUserOrderByLastModifiedDateDesc(user);
        return userAddresses.stream()
                .max(Comparator.comparing(Address::getLastModifiedDate))
                .orElseThrow(() -> new GenericApiException(ErrorCode.USER_ADDRESS_NOT_FOUND));
    }

    private Address findOrCreateAddress(User user, AddressDTO.Request address) {
        return addressRepository.findByUserAndStreetAndCityAndAndCountry(
                user, address.getStreet(), address.getCity(), address.getCountry()
        ).orElseGet(() -> createAndSaveNewAddress(user, address));
    }

    private Restaurant findAvailableRestaurant(Address deliveryAddress) {
        return Optional.ofNullable(findNearestAvailableRestaurant(deliveryAddress))
                .orElseThrow(() -> new GenericApiException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private List<OrderItem> buildOrderItems(List<OrderItemDTO.Request> orderItemDTOs) {
        return orderItemDTOs.stream()
                .map(this::createOrderItem)
                .toList();
    }

    private OrderItem createOrderItem(OrderItemDTO.Request itemDto) {
        Food food = foodRepository.findById(itemDto.getFoodId())
                .orElseThrow(() -> new GenericApiException(ErrorCode.FOOD_NOT_FOUND));

        return OrderItem.builder()
                .food(food)
                .quantity(itemDto.getQuantity())
                .price(Optional.ofNullable(food.getPrice()).orElse(BigDecimal.ZERO))
                .build();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createNewOrder(User user, Restaurant restaurant, Address deliveryAddress,
                                 List<OrderItem> orderItems, BigDecimal totalAmount) {
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .deliveryAddress(deliveryAddress)
                .totalAmount(totalAmount)
                .orderTime(now)
                .estimatedDeliveryTime(now.plusMinutes(15))
                .status(OrderStatus.DELIVERING)
                .orderItems(orderItems)
                .build();
        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }

    private void markRestaurantAndCourierAsBusy(Restaurant restaurant, Order order) {
        restaurantAvailabilityService.markRestaurantAsBusy(restaurant.getId(), order.getId(), order.getEstimatedDeliveryTime());
        restaurant.markAsBusy();
        restaurant.getCourier().markAsBusy();
        restaurantRepository.save(restaurant);
    }

    private Restaurant findNearestAvailableRestaurant(Address deliveryLocation) {
        LOG.info("Finding nearest available restaurant");

        List<Restaurant> availableRestaurants = restaurantRepository.findByAvailableAndActive(true, true);

        if (availableRestaurants.isEmpty()) {
            LOG.error("No available restaurants found");
            throw new GenericApiException(ErrorCode.RESTAURANT_UNAVAILABLE_FOR_DELIVERY);
        }

        return availableRestaurants.stream()
                .filter(r -> r.getAddress() != null)
                .min(Comparator.comparingDouble(r -> r.getAddress().distanceTo(deliveryLocation)))
                .orElseThrow(() -> new GenericApiException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private Address createAndSaveNewAddress(User user, AddressDTO.Request request) {
        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setCountry(request.getCountry());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        return addressRepository.save(address);
    }
}
