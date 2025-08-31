package dev.account.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.BaseWebIntegrationTest;
import dev.account.dto.AdminUserDTO;
import dev.account.mapper.UserMapper;
import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import dev.account.user.UserAccountService;
import dev.security.AuthoritiesConstants;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
public class UserResourceIT extends BaseWebIntegrationTest {

    private static final Long DEFAULT_ID = 1L;

    private static final String DEFAULT_EMAIL = "johndoe@localhost.com";
    private static final String UPDATED_EMAIL = "foody@localhost.com";

    private static final String DEFAULT_FULLNAME = "john doe";
    private static final String UPDATED_FULLNAME = "john doe updated";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    ObjectMapper om;

    @Autowired
    UserMapper userMapper;

    @Autowired
    EntityManager em;

    @Autowired
    CacheManager cacheManager;

    private User user;

    private Long numberOfUsers;

    /**
     * Create a User.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static User createEntity() {
        var persistUser = new User();
        persistUser.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        persistUser.setPassword(RandomStringUtils.randomAlphanumeric(60));
        persistUser.setActivated(true);
        persistUser.setFullname(DEFAULT_FULLNAME);
        persistUser.setLangKey(DEFAULT_LANGKEY);
        return persistUser;
    }

    /**
     * Setups the database with one user.
     */
    public static User initTestUser() {
        User persistUser = createEntity();
        persistUser.setEmail(DEFAULT_EMAIL);
        return persistUser;
    }

    @BeforeEach
    public void countUsers() {
        numberOfUsers = userAccountRepository.count();
    }

    @BeforeEach
    public void initTest() {
        user = initTestUser();
    }

    @AfterEach
    public void cleanupAndCheck() {
        cacheManager
                .getCacheNames()
                .stream()
                .map(cacheName -> this.cacheManager.getCache(cacheName))
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
        userAccountService.deleteUser(DEFAULT_EMAIL);
        userAccountService.deleteUser(UPDATED_EMAIL);
        userAccountService.deleteUser(user.getEmail());
        userAccountService.deleteUser("anotherlogin@e.com");
        assertThat(userAccountRepository.count()).isEqualTo(numberOfUsers);
        numberOfUsers = null;
    }

    @Test
    @Transactional
    void createUser() throws Exception {
        // Create the User
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setFullname(DEFAULT_FULLNAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setActivated(true);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        AdminUserDTO returnedUserDTO = om.readValue(
                mockMvc
                        .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                                .content(toJSON(userDTO)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                AdminUserDTO.class
        );

        User convertedUser = userMapper.toUser(returnedUserDTO);
        // Validate the returned User
        assertThat(convertedUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(convertedUser.getFullname()).isEqualTo(DEFAULT_FULLNAME);
        assertThat(convertedUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
    }

    @Test
    @Transactional
    void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userAccountRepository.findAll().size();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(DEFAULT_ID);
        userDTO.setFullname(DEFAULT_FULLNAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setActivated(true);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // An entity with an existing ID cannot be created, so this API call must fail
        mockMvc
                .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(userDTO)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userAccountRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userAccountRepository.findAll().size();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setFullname(DEFAULT_FULLNAME);
        userDTO.setEmail(DEFAULT_EMAIL); // this email should already be used
        userDTO.setActivated(true);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        mockMvc
                .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(userDTO)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    void getAllUsers() throws Exception {
        // Initialize the database
        userAccountRepository.saveAndFlush(user);

        // Get all the users
        mockMvc
                .perform(get("/api/admin/users?sort=id,desc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].fullname").value(hasItem(DEFAULT_FULLNAME)))
                .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
                .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    @Transactional
    void getUser() throws Exception {
        // Initialize the database
        userAccountRepository.saveAndFlush(user);

        assertThat(cacheManager.getCache(UserAccountRepository.USERS_BY_EMAIL_CACHE).get(user.getEmail())).isNull();

        // Get the user
        mockMvc
                .perform(get("/api/admin/users/{email}", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.fullname").value(DEFAULT_FULLNAME))
                .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
                .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));

        assertThat(cacheManager.getCache(UserAccountRepository.USERS_BY_EMAIL_CACHE).get(user.getEmail())).isNotNull();
    }

    @Test
    @Transactional
    void getNonExistingUser() throws Exception {
        mockMvc.perform(get("/api/admin/users/unknown")).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateUser() throws Exception {
        // Initialize the database
        userAccountRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userAccountRepository.findAll().size();

        // Update the user
        User updatedUser = userAccountRepository.findById(user.getId()).orElseThrow();

        var userDTO = new AdminUserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setFullname(UPDATED_FULLNAME);
        userDTO.setEmail(UPDATED_EMAIL);
        userDTO.setActivated(updatedUser.isActivated());
        userDTO.setLangKey(UPDATED_LANGKEY);
        userDTO.setCreatedBy(updatedUser.getCreatedBy());
        userDTO.setCreatedDate(updatedUser.getCreatedDate());
        userDTO.setLastModifiedBy(updatedUser.getLastModifiedBy());
        userDTO.setLastModifiedDate(updatedUser.getLastModifiedDate());
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        mockMvc
                .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(userDTO)))
                .andExpect(status().isOk());

        // Validate the User in the database
        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            User testUser = users.stream().filter(usr -> usr.getId().equals(updatedUser.getId())).findFirst().orElseThrow();
            assertThat(testUser.getFullname()).isEqualTo(UPDATED_FULLNAME);
            assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
            assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
        });
    }

    @Test
    @Transactional
    void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userAccountRepository.saveAndFlush(user);

        var anotherUser = new User();
        anotherUser.setPassword(RandomStringUtils.randomAlphanumeric(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("foody@localhost.com");
        anotherUser.setFullname("java spring");
        anotherUser.setLangKey("en");
        userAccountRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userAccountRepository.findById(user.getId()).orElseThrow();

        var userDTO = new AdminUserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setFullname(updatedUser.getFullname());
        userDTO.setEmail("foody@localhost.com"); // this email should already be used by anotherUser
        userDTO.setActivated(updatedUser.isActivated());
        userDTO.setLangKey(updatedUser.getLangKey());
        userDTO.setCreatedBy(updatedUser.getCreatedBy());
        userDTO.setCreatedDate(updatedUser.getCreatedDate());
        userDTO.setLastModifiedBy(updatedUser.getLastModifiedBy());
        userDTO.setLastModifiedDate(updatedUser.getLastModifiedDate());
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        mockMvc
                .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    void deleteUser() throws Exception {
        // Initialize the database
        userAccountRepository.saveAndFlush(user);
        int databaseSizeBeforeDelete = userAccountRepository.findAll().size();

        // Delete the user
        mockMvc
                .perform(delete("/api/admin/users/{login}", user.getEmail()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(cacheManager.getCache(UserAccountRepository.USERS_BY_EMAIL_CACHE).get(user.getEmail())).isNull();

        // Validate the database is empty
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeDelete - 1));
    }

    private void assertPersistedUsers(Consumer<List<User>> userAssertion) {
        userAssertion.accept(userAccountRepository.findAll());
    }
}
