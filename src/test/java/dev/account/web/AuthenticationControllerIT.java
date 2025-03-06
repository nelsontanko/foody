package dev.account.web;

import dev.BaseWebIntegrationTest;
import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import dev.account.web.vm.LoginVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;

/**
 * Integration tests for the {@link AuthenticationController} REST controller.
 */
@Transactional
class AuthenticationControllerIT extends BaseWebIntegrationTest {

    @Autowired UserAccountRepository userAccountRepository;

    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void testAuthorize() throws Exception {
        var user = new User();
        user.setEmail("user-jwt-controller@example.com");
        user.setActivated(true);
        user.setPassword(passwordEncoder.encode("test"));

        userAccountRepository.saveAndFlush(user);

        var login = new LoginVM();
        login.setEmail("user-jwt-controller@example.com");
        login.setPassword("test");

        this.mockMvc
                .perform(post("/api/account/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isString())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(header().string("Authorization", not(nullValue())))
                .andExpect(header().string("Authorization", not(is(emptyString()))));
    }

    @Test
    void testAuthorizeWithRememberMe() throws Exception {
        var user = new User();
        user.setEmail("user-jwt-controller-remember-me@email.com");
        user.setPassword(passwordEncoder.encode("test"));
        user.setActivated(true);

        userAccountRepository.saveAndFlush(user);

        var login = new LoginVM();
        login.setEmail("user-jwt-controller-remember-me@email.com");
        login.setPassword("test");
        login.setRememberMe(true);

        this.mockMvc
                .perform(post("/api/account/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isString())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(header().string("Authorization", not(nullValue())))
                .andExpect(header().string("Authorization", not(is(emptyString()))));
    }

    @Test
    void testAuthorizeFails() throws Exception {
        var login = new LoginVM();
        login.setEmail("wrong-user@email.com");
        login.setPassword("wrong password");

        this.mockMvc
                .perform(post("/api/account/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content(toJSON(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.access_token").doesNotExist())
                .andExpect(header().doesNotExist("Authorization"));
    }
}
