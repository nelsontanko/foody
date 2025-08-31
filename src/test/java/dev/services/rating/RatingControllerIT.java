package dev.services.rating;

import dev.BaseWebIntegrationTest;
import dev.WithFoodyUser;
import dev.account.user.User;
import dev.services.TestDataHelper;
import dev.services.food.Food;
import dev.services.rating.RatingDTO.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 */
class RatingControllerIT extends BaseWebIntegrationTest {

    @Autowired TestDataHelper testDataHelper;

    private Food testFood;

    @BeforeEach
    void setUp() {
        testFood = testDataHelper.createFood();
    }

    @AfterEach
    void cleanUp() {
        testDataHelper.clearData();
    }

    @Test
    @WithFoodyUser
    void addNewRating_Success(User testUser) throws Exception {
        Request request = new Request(testFood.getId(), 5);

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.foodId").value(testFood.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @WithFoodyUser
    void updateExistingRating_Success(User testUser) throws Exception {
        testDataHelper.createRating(3, testFood, testUser);

        Request request = new Request(testFood.getId(), 4);

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void addRating_UserNotAuthenticated_ReturnsError() throws Exception {
        Request request = new Request(testFood.getId(), 4);

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithFoodyUser
    void addRating_InvalidValue_ReturnsBadRequest() throws Exception {
        Request request = new Request(testFood.getId(), 6); // Invalid rating

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Rating must be between 1 and 5"));
    }


}
