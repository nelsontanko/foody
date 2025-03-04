package dev.services.order;

import dev.BaseWebIntegrationTest;
import dev.account.user.AddressDTO;
import dev.services.TestDataHelper;
import dev.services.food.Food;
import dev.services.restaurant.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 */
@Disabled(value = "Tests run in isolation")
class OrderControllerIT extends BaseWebIntegrationTest {

    @Autowired TestDataHelper testDataHelper;
    @Autowired OrderRepository orderRepository;
    @Autowired RestaurantRepository restaurantRepository;

    private Food testFood;

    @BeforeEach
    void setUp() {
        testDataHelper.createUser("user@example.com", Set.of("ROLE_USER"));
        testDataHelper.createUser("newuser@example.com", Set.of("ROLE_USER"));

        testDataHelper.createRestaurant("Gummy Bites", true, true, 88.99, 330.98);
        testFood = testDataHelper.createFood();
    }


    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_Success() throws Exception {
        OrderDTO.Request request = new OrderDTO.Request();
        request.setDeliveryAddress(new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060));
        request.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 2)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").value("25.98")) // 2 x 12.99
                .andExpect(jsonPath("$.status").value("DELIVERING"));

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_WithoutAddress_UsesLastAddress() throws Exception {
        // First Order (Explicit Address Provided)
        OrderDTO.Request firstRequest = new OrderDTO.Request();
        firstRequest.setDeliveryAddress(new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060));
        firstRequest.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(firstRequest)))
                .andExpect(status().isCreated());

        // Second Order (No Address Provided)
        OrderDTO.Request secondRequest = new OrderDTO.Request();
        secondRequest.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliveryAddress.street").value("123 Main St"));
    }


    @Test
    @WithMockUser(username = "newuser@example.com", authorities = {"ROLE_USER"})
    void createOrder_NoPreviousAddress_ShouldThrowException() throws Exception {
        OrderDTO.Request request = new OrderDTO.Request();
        request.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("api.user.addressNotFound"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_NoAvailableRestaurant_ShouldThrowException() throws Exception {
        restaurantRepository.deleteAll(); // Remove all restaurants

        OrderDTO.Request request = new OrderDTO.Request();
        request.setDeliveryAddress(new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060));
        request.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.restaurant.noRestaurantAvailableForDelivery"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_FoodNotFound_ShouldThrowException() throws Exception {
        OrderDTO.Request request = new OrderDTO.Request();
        request.setDeliveryAddress(new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060));
        request.setOrderItems(List.of(new OrderItemDTO.Request(9999L, 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.food.notFound"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_AssignsNearestRestaurant() throws Exception {
        testDataHelper.createRestaurant("Tasty Bites", true, true,40.7130, -74.0050);
        testDataHelper.createRestaurant("Food Haven", true, true,40.7306, -73.9352);
        testDataHelper.createRestaurant("Distant Deli",true, true, 40.7500, -73.8000);

        AddressDTO.Request deliveryAddress = new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060);

        OrderDTO.Request orderRequest = new OrderDTO.Request();
        orderRequest.setDeliveryAddress(deliveryAddress);
        orderRequest.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantName").value("Tasty Bites"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_WhenTwoRestaurantsAreEquallyNear_AssignsFirstOne() throws Exception {
        // Restaurants at the same distance
        testDataHelper.createRestaurant("Bistro A", true, true, 40.7135, -74.0055);
        testDataHelper.createRestaurant("Bistro B", true, true, 40.7135, -74.0055); // Same location

        AddressDTO.Request deliveryAddress = new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060);

        OrderDTO.Request orderRequest = new OrderDTO.Request();
        orderRequest.setDeliveryAddress(deliveryAddress);
        orderRequest.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantName").value("Bistro A"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void createOrder_WhenNearestRestaurantIsUnavailable_SkipsIt() throws Exception {
        testDataHelper.createRestaurant("Inactive Spot", true, false, 40.7130, -74.0050);

        testDataHelper.createRestaurant("Active Spot", true, true, 40.7306, -70.9352);

        AddressDTO.Request deliveryAddress = new AddressDTO.Request("123 Main St", "Abuja", "Nigeria", 40.7128, -74.0060);

        OrderDTO.Request orderRequest = new OrderDTO.Request();
        orderRequest.setDeliveryAddress(deliveryAddress);
        orderRequest.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantName").value("Active Spot"));
    }



    @Test
    void createOrder_Unauthorized_ReturnsForbidden() throws Exception {
        OrderDTO.Request request = new OrderDTO.Request();
        request.setDeliveryAddress(new AddressDTO.Request("123 Main St", "New York", "USA", 40.7128, -74.0060));
        request.setOrderItems(List.of(new OrderItemDTO.Request(testFood.getId(), 1)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isUnauthorized());
    }



}
