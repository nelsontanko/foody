package dev.services.restaurant;

import dev.BaseWebIntegrationTest;
import dev.services.TestDataHelper;
import dev.services.restaurant.RestaurantDTO.Request;
import dev.services.restaurant.RestaurantDTO.UpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 */
class RestaurantControllerIT extends BaseWebIntegrationTest {
    @Autowired RestaurantRepository restaurantRepository;
    @Autowired TestDataHelper testDataHelper;

    @BeforeEach
    void setUp() {
        testDataHelper.createUser("admin@example.com", Set.of("ROLE_ADMIN"));
        testDataHelper.createUser("user@example.com", Set.of("ROLE_USER"));
    }

    @AfterEach
    void cleanUp(){
        testDataHelper.clearData();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void createRestaurant_Success() throws Exception {
        Request restaurantRequest = testDataHelper.createRestaurant();

        mockMvc.perform(post("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(restaurantRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tasty Bites"))
                .andExpect(jsonPath("$.address.street").value("123 Main St"));

        assertThat(restaurantRepository.existsByName("Tasty Bites")).isTrue();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void createRestaurant_DuplicateAddress_ShouldThrowException() throws Exception {
        Request restaurantRequest = testDataHelper.createRestaurant();

        mockMvc.perform(post("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(restaurantRequest)))
                .andExpect(status().isCreated());

        Request restaurantRequest2 = testDataHelper.createRestaurant();
        mockMvc.perform(post("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(restaurantRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("api.restaurant.addressAlreadyExists"));
    }

    @Test
    void createRestaurant_Unauthorized_ReturnsForbidden() throws Exception {
        Request restaurantRequest = testDataHelper.createRestaurant();

        mockMvc.perform(post("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(restaurantRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void updateRestaurant_Success() throws Exception {
        Restaurant existingRestaurant = testDataHelper.createRestaurant("Tasty Bites", true, true, 78.99, 333.98);
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setName("Tasty Bites Updated");

        mockMvc.perform(patch("/api/restaurant/{restaurantId}", existingRestaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tasty Bites Updated"));

        Restaurant updatedRestaurant = restaurantRepository.findById(existingRestaurant.getId()).orElseThrow();
        assertThat(updatedRestaurant.getName()).isEqualTo("Tasty Bites Updated");
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void updateRestaurant_AddCourier_Success() throws Exception {
        Restaurant existingRestaurant = testDataHelper.createRestaurant("Tasty Bites", true, true, 78.99, 333.98);
        CourierDTO.UpdateRequest courierRequest = new CourierDTO.UpdateRequest();
        courierRequest.setName("New Tasty Bites Courier");
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setCourier(courierRequest);

        mockMvc.perform(patch("/api/restaurant/{restaurantId}", existingRestaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courier.name").value("New Tasty Bites Courier"));

        Restaurant updatedRestaurant = restaurantRepository.findById(existingRestaurant.getId()).orElseThrow();
        assertThat(updatedRestaurant.getCourier()).isNotNull();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void updateRestaurant_WithoutAvailableField_ShouldKeepPreviousValue() throws Exception {
        Restaurant existingRestaurant = testDataHelper.createRestaurant("Tasty Bites", true, true, 78.99, 333.98);
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setName("Tasty Bites Updated");

        mockMvc.perform(patch("/api/restaurant/{restaurantId}", existingRestaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tasty Bites Updated"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void updateRestaurant_NotFound_ShouldThrowException() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setName("Non-Existent Restaurant");

        mockMvc.perform(patch("/api/restaurant/{restaurantId}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.restaurant.notFound"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void getAllRestaurants_Success() throws Exception {
        testDataHelper.createRestaurant("Tasty Bites", true, true, 78.99, 333.98);
        testDataHelper.createRestaurant("Frosty Bites", true, false, 78.99, 333.98);
        testDataHelper.createRestaurant("Gummy Bites", false, false, 78.99, 333.98);

        mockMvc.perform(get("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].name").value("Tasty Bites"))
                .andExpect(jsonPath("$.content[1].name").value("Frosty Bites"))
                .andExpect(jsonPath("$.content[2].name").value("Gummy Bites"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void getRestaurantById_Success() throws Exception {
        Restaurant restaurant = testDataHelper.createRestaurant("Tasty Bites", true, true, 78.99, 333.98);

        mockMvc.perform(get("/api/restaurant/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tasty Bites"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void getRestaurantById_NotFound_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/api/restaurant/{restaurantId}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.restaurant.notFound"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void deleteRestaurant_Success() throws Exception {
        Restaurant restaurant = testDataHelper.createRestaurant("Tasty Bites", true, true, 78.99, 333.98);

        mockMvc.perform(delete("/api/restaurant/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Restaurant deletedRestaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertThat(deletedRestaurant.isActive()).isFalse();
        assertThat(deletedRestaurant.getCourier().isActive()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    void deleteRestaurant_NotFound_ReturnsError() throws Exception {
        mockMvc.perform(delete("/api/restaurant/{restaurantId}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.restaurant.notFound"));
    }

}
