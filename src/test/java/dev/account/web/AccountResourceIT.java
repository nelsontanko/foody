package dev.account.web;

import dev.BaseWebIntegrationTest;
import dev.account.WithUnauthenticatedMockUser;
import dev.account.dto.*;
import dev.account.mapper.AuthorityMapper;
import dev.account.user.*;
import dev.account.web.vm.KeyAndPasswordVM;
import dev.account.web.vm.ManagedUserVM;
import dev.core.config.Constants;
import dev.security.AuthoritiesConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
class AccountResourceIT extends BaseWebIntegrationTest {

    static final String TEST_USER_EMAIL = "test@example.com";

    @Autowired UserAccountRepository userAccountRepository;

    @Autowired AuthorityRepository authorityRepository;

    @Autowired UserAccountService userAccountService;

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired AuthorityMapper authorityMapper;
    private Long numberOfUsers;

    @BeforeEach
    public void countUsers() {
        numberOfUsers = userAccountRepository.count();
    }

    @AfterEach
    public void cleanupAndCheck() {
        assertThat(userAccountRepository.count()).isEqualTo(numberOfUsers);
        numberOfUsers = null;
    }

    @Test
    @WithUnauthenticatedMockUser
    void testNonAuthenticatedUser() throws Exception {
        this.mockMvc
                .perform(get("/api/account/authenticate").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(TEST_USER_EMAIL)
    void testAuthenticatedUser() throws Exception {
        this.mockMvc
                .perform(get("/api/account/authenticate").with(request -> request).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(TEST_USER_EMAIL));
    }

    @Test
    @WithMockUser(TEST_USER_EMAIL)
    void testGetExistingAccount() throws Exception {
        Set<String> authorities = new HashSet<>();
        authorities.add(AuthoritiesConstants.ADMIN);

        AdminUserDTO user = new AdminUserDTO();
        user.setEmail(TEST_USER_EMAIL);
        user.setFullname("john doe");
        user.setLangKey("en");
        user.setAuthorities(authorities);
        userAccountService.createUser(user);

        this.mockMvc
                .perform(get("/api/account/me").accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.email").value(TEST_USER_EMAIL))
                .andExpect(jsonPath("$.fullname").value("john doe"));
        userAccountService.deleteUser(TEST_USER_EMAIL);
    }

    @Test
    void testGetUnknownAccount() throws Exception {
        this.mockMvc.perform(get("/api/account").accept(MediaType.APPLICATION_PROBLEM_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setEmail("test-register-valid@example.com");
        validUser.setPassword("password");
        validUser.setFullname("Alice Won");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        assertThat(userAccountRepository.findOneByEmailIgnoreCase("test-register-valid@example.com")).isEmpty();

        this.mockMvc
                .perform(post("/api/account/register").contentType(APPLICATION_JSON)
                        .content(toJSON(validUser)))
                .andExpect(status().isCreated())
                .andDo(print());

        assertThat(userAccountRepository.findOneByEmailIgnoreCase("test-register-valid@example.com")).isPresent();

        userAccountService.deleteUser("test-register-valid@example.com");
    }

    @Test
    @Transactional
    void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setEmail("funkyexample.com"); // <-- invalid email
        invalidUser.setPassword("password");
        invalidUser.setFullname("Funky One");
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        this.mockMvc
                .perform(post("/api/account/register").contentType(APPLICATION_JSON)
                        .content(toJSON(invalidUser)))
                .andExpect(status().isBadRequest());

        Optional<User> user = userAccountRepository.findOneByEmailIgnoreCase("funkyexample.com");
        assertThat(user).isEmpty();
    }

    static Stream<ManagedUserVM> invalidUsers() {
        return Stream.of(
                createInvalidUser("bobexample", "password", "Bob" ), // <-- invalid
                createInvalidUser("bob@example.com", "123", "Bob" ), // password with only 3 digits
                createInvalidUser("bob@example.com", null, "Bob D") // invalid null password
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUsers")
    @Transactional
    void testRegisterInvalidUsers(ManagedUserVM invalidUser) throws Exception {
        this.mockMvc
                .perform(post("/api/account/register").contentType(APPLICATION_JSON)
                        .content(toJSON(invalidUser)))
                .andExpect(status().isBadRequest());

        Optional<User> user = userAccountRepository.findOneByEmailIgnoreCase("bob@email.com");
        assertThat(user).isEmpty();
    }

    private static ManagedUserVM createInvalidUser(String email, String password, String fullname) {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setPassword(password);
        invalidUser.setFullname(fullname);
        invalidUser.setEmail(email);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        return invalidUser;
    }

    @Test
    @Transactional
    void testRegisterDuplicateEmail() throws Exception {
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setPassword("password");
        firstUser.setFullname("Alice Test");
        firstUser.setEmail("test-register-duplicate-email@example.com");
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Register first user
        this.mockMvc
                .perform(post("/api/account/register").contentType(APPLICATION_JSON)
                        .content(toJSON(firstUser)))
                .andExpect(status().isCreated());

        Optional<User> testUser1 = userAccountRepository.findOneByEmailIgnoreCase("test-register-duplicate-email@example.com");
        assertThat(testUser1).isPresent();

        // Duplicate email - with uppercase email address
        ManagedUserVM userWithUpperCaseEmail = new ManagedUserVM();
        userWithUpperCaseEmail.setId(firstUser.getId());
        userWithUpperCaseEmail.setPassword(firstUser.getPassword());
        userWithUpperCaseEmail.setFullname(firstUser.getFullname());
        userWithUpperCaseEmail.setEmail("TEST-register-duplicate-email@example.com");
        userWithUpperCaseEmail.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register 4th user
        this.mockMvc
                .perform(post("/api/register").contentType(APPLICATION_JSON)
                        .content(toJSON(userWithUpperCaseEmail)))
                .andExpect(status().is4xxClientError());

        userAccountService.deleteUser("TEST-register-duplicate-email@example.com");
    }

    @Test
    @Transactional
    void testRegisterDuplicateMobileNumber() throws Exception {
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setPassword("password");
        firstUser.setFullname("Alice Test");
        firstUser.setEmail("test-email@example.com");
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Register first user
        this.mockMvc
                .perform(post("/api/account/register").contentType(APPLICATION_JSON)
                        .content(toJSON(firstUser)))
                .andExpect(status().isCreated());

        Optional<User> testUser1 = userAccountRepository.findOneByEmailIgnoreCase("test-email@example.com");
        assertThat(testUser1).isPresent();

        // Duplicate phone - with intl code
        ManagedUserVM userMobileNumberWithIntlCode = new ManagedUserVM();
        userMobileNumberWithIntlCode.setId(firstUser.getId());
        userMobileNumberWithIntlCode.setPassword(firstUser.getPassword());
        userMobileNumberWithIntlCode.setFullname(firstUser.getFullname());
        userMobileNumberWithIntlCode.setEmail(firstUser.getEmail());
        userMobileNumberWithIntlCode.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register third user
        this.mockMvc
                .perform(post("/api/account/register").contentType(APPLICATION_JSON)
                        .content(toJSON(userMobileNumberWithIntlCode)))
                .andExpect(status().is4xxClientError());

        userAccountService.deleteUser("test-email@example.com");
    }


    @Test
    @Transactional
    @WithMockUser("save-account@example.com")
    void testUpdateUserAccount() throws Exception {
        User user = new User();
        user.setEmail("save-account@example.com");
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        userAccountRepository.saveAndFlush(user);

        UserUpdateDTO updateVM = new UserUpdateDTO();
        updateVM.setFullname("fullname");

        this.mockMvc
                .perform(patch("/api/account").contentType(APPLICATION_JSON)
                        .content(toJSON(updateVM)))
                .andExpect(status().isOk());

        User updatedUser = userAccountRepository.findOneWithAuthoritiesByEmailIgnoreCase(user.getEmail()).orElse(null);
        assertThat(updatedUser.getFullname()).isEqualTo(updateVM.getFullname());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getAuthorities()).isEmpty();

        userAccountService.deleteUser("save-account@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-pw-wrong-existing-password@example.com")
    void testChangePasswordWithWrongExistingPassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setEmail("change-pw-wrong-existing-password@example.com");
        userAccountRepository.saveAndFlush(user);

        this.mockMvc
                .perform(post("/api/account/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(toJSON(new PasswordChangeDTO("1" + currentPassword, "new password"))))
                .andExpect(status().isBadRequest());

        User updatedUser = userAccountRepository
                .findOneByEmailIgnoreCase("change-pw-wrong-existing-password@example.com").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();

        userAccountService.deleteUser("change-pw-wrong-existing-password@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password@example.com")
    void testChangePasswordSuccess() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setEmail("change-password@example.com");
        userAccountRepository.saveAndFlush(user);

        this.mockMvc
                .perform(post("/api/account/change-password")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(new PasswordChangeDTO(currentPassword, "new password")))
                )
                .andExpect(status().isOk());

        User updatedUser = userAccountRepository.findOneByEmailIgnoreCase("change-password@example.com").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();

        userAccountService.deleteUser("change-password@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small@example.com")
    void testChangePasswordTooSmall() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setEmail("change-password-too-small@example.com");
        userAccountRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        this.mockMvc
                .perform(post("/api/account/change-password")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(new PasswordChangeDTO(currentPassword, newPassword)))
                )
                .andExpect(status().isBadRequest());

        User updatedUser = userAccountRepository.findOneByEmailIgnoreCase("change-password-too-small@example.com").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userAccountService.deleteUser("change-password-too-small@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long@example.com")
    void testChangePasswordTooLong() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setEmail("change-password-too-long@example.com");
        userAccountRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        this.mockMvc
                .perform(post("/api/account/change-password")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(new PasswordChangeDTO(currentPassword, newPassword)))
                )
                .andExpect(status().isBadRequest());

        User updatedUser = userAccountRepository.findOneByEmailIgnoreCase("change-password-too-long@example.com").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userAccountService.deleteUser("change-password-too-long@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty@example.com")
    void testChangePasswordEmpty() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setEmail("change-password-empty@example.com");
        userAccountRepository.saveAndFlush(user);

        this.mockMvc
                .perform(
                        post("/api/account/change-password")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(new PasswordChangeDTO(currentPassword, "")))
                )
                .andExpect(status().isBadRequest());

        User updatedUser = userAccountRepository.findOneByEmailIgnoreCase("change-password-empty@example.com").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userAccountService.deleteUser("change-password-empty@example.com");
    }

    @Test
    @Transactional
    void testRequestPasswordResetInit() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setEmail("password-reset@example.com");
        userAccountRepository.saveAndFlush(user);

        this.mockMvc
                .perform(post("/api/account/reset-password/init")
                        .contentType(APPLICATION_JSON)
                        .content(toJSON(new ResetEmailDTO("password-reset@example.com"))))
                .andExpect(status().isOk());

        userAccountService.deleteUser("password-reset@example.com");
    }

    @Test
    void testRequestPasswordResetWrongEmail() throws Exception {
        this.mockMvc
                .perform(post("/api/account/reset-password/init")
                        .contentType(APPLICATION_JSON)
                        .content(toJSON(new ResetEmailDTO("password-reset-wrong@example.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void testFinishPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setEmail("finish-password-reset@example.com");
        user.setResetDate(LocalDateTime.now().plusSeconds(60));
        user.setResetKey("reset key");
        userAccountRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("new password");

        this.mockMvc
                .perform(post("/api/account/reset-password/finish")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(keyAndPassword))
                )
                .andExpect(status().isOk());

        User updatedUser = userAccountRepository.findOneByEmailIgnoreCase(user.getEmail()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isTrue();

        userAccountService.deleteUser("finish-password-reset@example.com");
    }

    @Test
    @Transactional
    void testFinishPasswordResetTooSmall() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setEmail("finish-password-reset-too-small@example.com");
        user.setResetDate(LocalDateTime.now().plusSeconds(60));
        user.setResetKey("reset key too small");
        userAccountRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        this.mockMvc
                .perform(post("/api/account/reset-password/finish")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(keyAndPassword))
                )
                .andExpect(status().isBadRequest());

        User updatedUser = userAccountRepository.findOneByEmailIgnoreCase(user.getEmail()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();

        userAccountService.deleteUser("finish-password-reset-too-small@example.com");
    }

    @Test
    @Transactional
    void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        this.mockMvc
                .perform(
                        post("/api/account/reset-password/finish")
                                .contentType(APPLICATION_JSON)
                                .content(toJSON(keyAndPassword))
                )
                .andExpect(status().isInternalServerError());
    }
}
