package dev.services.comment;

import dev.BaseWebIntegrationTest;
import dev.account.user.User;
import dev.services.TestDataHelper;
import dev.services.comment.CommentDTO.Request;
import dev.services.food.Food;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 */
class CommentControllerIT extends BaseWebIntegrationTest {

    @Autowired TestDataHelper testDataHelper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataHelper.createUser("user@example.com", Set.of("ROLE_USER"));
    }

    @AfterEach
    void cleanUp(){
        testDataHelper.clearData();
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void addComment_Success() throws Exception {
        Food food = testDataHelper.createFood();
        Request request = Request.builder()
                .content("Hello comet")
                .foodId(food.getId())
                .build();

        mockMvc.perform(post("/api/comment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJSON(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello comet"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.foodId").value(food.getId()));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void addComment_WhenFoodNotFound_ShouldThrowException() throws Exception {
        Request request = Request.builder()
                .content("Hello comet")
                .foodId(222L)
                .build();

        mockMvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.food.notFound"));

    }

    @Test
    @WithMockUser(username = "user-not-found@example.com", authorities = {"ROLE_USER"})
    void addComment_WhenNotUser_ShouldThrowException() throws Exception {
        Food food = testDataHelper.createFood();
        Request request = Request.builder()
                .content("Hello comet")
                .foodId(food.getId())
                .build();

        mockMvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("api.user.notFound"));

    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void getCommentsByFoodId_Success_WithDefaultPagination() throws Exception {
        Food food = testDataHelper.createFood();
        testDataHelper.createFoodsWithComments(food, testUser);

        mockMvc.perform(get("/api/comment/food/{foodId}", food.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].content").value("Delicious"))
                .andExpect(jsonPath("$.comments[0].foodId").value(food.getId()))
                .andExpect(jsonPath("$.comments[0].userId").value(testUser.getId()));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
    void getCommentsByFoodId_Success_WithProvidedCommentSize() throws Exception {
        Food food = testDataHelper.createFood();
        testDataHelper.createFoodsWithComments(food, testUser);

        mockMvc.perform(get("/api/comment/food/{foodId}?size=2", food.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2))
                .andExpect(jsonPath("$.comments[0].content").value("Delicious"))
                .andExpect(jsonPath("$.comments[1].content").value("Very nice food"))
                .andExpect(jsonPath("$.comments[0].foodId").value(food.getId()))
                .andExpect(jsonPath("$.comments[0].userId").value(testUser.getId()));
    }
}
