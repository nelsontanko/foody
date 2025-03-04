package dev.services.food;

import dev.BaseWebIntegrationTest;
import dev.account.user.User;
import dev.services.TestDataHelper;
import dev.services.food.FoodDTO.Request;
import dev.services.food.FoodDTO.UpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "user@example.com", authorities = {"ROLE_ADMIN"})
class FoodControllerIT extends BaseWebIntegrationTest {

    @Autowired FoodRepository foodRepository;
    @Autowired TestDataHelper testDataHelper;

    private User testUser;

    @BeforeEach
    void setUp() {
       testUser = testDataHelper.createUser("user@example.com", Set.of("ROLE_ADMIN"));
    }

    @AfterEach
    void cleanUp(){
        testDataHelper.clearData();
    }

    @Test
    void addFood_Success() throws Exception {
        Request foodRequest = Request.builder()
                .name("Pizza Margherita")
                .description("Classic Italian pizza")
                .price(new BigDecimal("12.99"))
                .build();

        mockMvc.perform(post("/api/food")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(foodRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Pizza Margherita")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void addFood_WithExistingFoodName_ShouldThrowException() throws Exception {
        testDataHelper.createFood();
        Request foodRequest = Request.builder()
                .name("Pizza Margherita")
                .description("Classic Italian pizza")
                .price(new BigDecimal("12.99"))
                .build();

        mockMvc.perform(post("/api/food")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(foodRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFood_Success() throws Exception {
        Food food = testDataHelper.createFood();

        UpdateRequest request = new UpdateRequest();
        request.setName("Food update");
        request.setAvailable(false);
        request.setDescription("Food description");

        mockMvc.perform(patch("/api/food/{foodId}", food.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Food update")))
                .andExpect(jsonPath("$.description", is("Food description")));
    }

    @Test
    void updateFood_WithExistingFoodName_ShouldThrowException() throws Exception {
        testDataHelper.createFood("Diary blue", "Great product", BigDecimal.TWO,true);
        Food food = testDataHelper.createFood();

        UpdateRequest request = new UpdateRequest();
        request.setName("Diary blue");
        request.setAvailable(false);
        request.setDescription("Food description");

        mockMvc.perform(patch("/api/food/{foodId}", food.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFood_Success() throws Exception {
        Food food = testDataHelper.createFood();

        mockMvc.perform(delete("/api/food/{foodId}", food.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Food deletedFood = foodRepository.findById(food.getId()).orElseThrow();
        assertThat(deletedFood.isActive()).isFalse();
    }

    @Test
    void getFoodById_Success() throws Exception {
        Food food = testDataHelper.createFood();
        mockMvc.perform(get("/api/food/{foodId}", food.getId())
                        .param("comment_count", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Pizza Margherita")));
    }

    @Test
    void getFoodById_Success_WithDefaultComment() throws Exception {
        Food food = testDataHelper.createFood();
        testDataHelper.createFoodsWithComments(food, testUser);

        mockMvc.perform(get("/api/food/{foodId}", food.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Pizza Margherita")))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].content", is("Very nice food")))
                .andDo(print());
    }

    @Test
    void getFoodById_Success_WithProvidedCommentCount() throws Exception {
        Food food = testDataHelper.createFood();
        testDataHelper.createFoodsWithComments(food, testUser);
        mockMvc.perform(get("/api/food/{foodId}", food.getId())
                        .param("comment_count", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Pizza Margherita")))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2))
                .andExpect(jsonPath("$.comments[0].content", is("Very nice food")))
                .andExpect(jsonPath("$.comments[1].content", is("Delicious")))
                .andDo(print());
    }

    @Test
    void getFoodById_NotFound() throws Exception {
        mockMvc.perform(get("/api/food/{foodId}", 222))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("api.food.notFound")));
    }

    @Test
    void getAllFood_Success_WithDefaultPaginationAndFilters() throws Exception {
        testDataHelper.createMultipleFoods();

        mockMvc.perform(get("/api/food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].name", is("Banana Flush")))
                .andExpect(jsonPath("$.content[1].name", is("Olive Orange")))
                .andExpect(jsonPath("$.content[0].price", is(50.55)));

    }

    @Test
    void getAllFood_Success_WithPagination() throws Exception {
        testDataHelper.createMultipleFoods();

        mockMvc.perform(get("/api/food?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].name", is("Banana Flush")))
                .andExpect(jsonPath("$.content[1].name", is("Olive Orange")))
                .andExpect(jsonPath("$.content[0].price", is(50.55)));

    }

    @Test
    void getAllFood_Success_WithFilters() throws Exception {
        testDataHelper.createMultipleFoods();

        mockMvc.perform(get("/api/food?available=true&min_price=50&max_price=300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].name", is("Banana Flush")))
                .andExpect(jsonPath("$.content[0].price", is(50.55)));

    }
}