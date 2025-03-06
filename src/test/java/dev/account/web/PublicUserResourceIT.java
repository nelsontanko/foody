package dev.account.web;


import dev.BaseWebIntegrationTest;
import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import dev.account.user.UserAccountService;
import dev.security.AuthoritiesConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link PublicUserResource} REST controller.
 */
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class PublicUserResourceIT extends BaseWebIntegrationTest {

    @Autowired
    UserAccountRepository userRepository;

    @Autowired
    UserAccountService userService;

    @Autowired
    CacheManager cacheManager;

    private User user;
    private Long numberOfUsers;

    @BeforeEach
    public void countUsers() {
        numberOfUsers = userRepository.count();
    }

    @BeforeEach
    public void initTest() {
        user = UserResourceIT.initTestUser();
    }

    @AfterEach
    public void cleanupAndCheck() {
        cacheManager
            .getCacheNames()
            .stream()
            .map(cacheName -> this.cacheManager.getCache(cacheName))
            .filter(Objects::nonNull)
            .forEach(Cache::clear);
        userService.deleteUser(user.getEmail());
        assertThat(userRepository.count()).isEqualTo(numberOfUsers);
        numberOfUsers = null;
    }

    @Test
    @Transactional
    void getAllPublicUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        mockMvc
            .perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[?(@.id == %d)].email", user.getId()).value(user.getEmail()))
            .andExpect(jsonPath("$.[?(@.id == %d)].keys()", user.getId()).value(Set.of("id", "email")))
            .andExpect(jsonPath("$.[*].email").hasJsonPath())
            .andExpect(jsonPath("$.[*].langKey").doesNotHaveJsonPath());
    }

    @Test
    @Transactional
    void getAllUsersSortedByParameters() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/users?sort=resetKey,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/users?sort=password,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        mockMvc
            .perform(get("/api/users?sort=resetKey,desc&sort=id,desc").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }
}
