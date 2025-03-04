package dev.account.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.BaseWebIntegrationTest;
import dev.account.user.Authority;
import dev.account.user.AuthorityRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static dev.account.AuthorityAsserts.assertAuthorityUpdatableFieldsEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AuthorityResource} REST controller.
 */
@WithMockUser(authorities = {"ROLE_ADMIN"})
public class AuthorityResourceIT extends BaseWebIntegrationTest {

    private static final String ENTITY_API_URL = "/api/authorities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{name}";

    @Autowired ObjectMapper om;
    @Autowired AuthorityRepository authorityRepository;

    @Autowired EntityManager em;

    private Authority authority;

    private Authority insertedAuthority;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authority createEntity() {
        return new Authority().name(UUID.randomUUID().toString());
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authority createUpdatedEntity() {
        return new Authority().name(UUID.randomUUID().toString());
    }

    @BeforeEach
    public void initTest() {
        authority = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedAuthority != null) {
            authorityRepository.delete(insertedAuthority);
            insertedAuthority = null;
        }
    }

    @Test
    @Transactional
    void createAuthority() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Authority
        var returnedAuthority = om.readValue(
                mockMvc
                        .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON)
                                .content(toJSON(authority)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                Authority.class
        );

        // Validate the Authority in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertAuthorityUpdatableFieldsEquals(returnedAuthority, getPersistedAuthority(returnedAuthority));

        insertedAuthority = returnedAuthority;
    }

    @Test
    @Transactional
    void createAuthorityWithExistingId() throws Exception {
        // Create the Authority with an existing ID
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(authority)))
                .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAuthorities() throws Exception {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        // Get all the authorityList
        mockMvc
                .perform(get(ENTITY_API_URL + "?sort=name,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].name").value(hasItem(authority.getName())));
    }

    @Test
    @Transactional
    void getAuthority() throws Exception {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        // Get the authority
        mockMvc
                .perform(get(ENTITY_API_URL_ID, authority.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name").value(authority.getName()));
    }

    @Test
    @Transactional
    void getNonExistingAuthority() throws Exception {
        // Get the authority
        mockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void deleteAuthority() throws Exception {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the authority
        mockMvc
                .perform(delete(ENTITY_API_URL_ID, authority.getName()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return authorityRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Authority getPersistedAuthority(Authority authority) {
        return authorityRepository.findById(authority.getName()).orElseThrow();
    }
}
