package dev.security;

import dev.FoodyIntegrationTest;
import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import dev.account.user.UserAccountService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Integrations tests for {@link DomainUserDetailsService}.
 */
@Transactional
@FoodyIntegrationTest
class DomainUserDetailsServiceIT {

    private static final String USER_ONE_EMAIL = "test-user-one@localhost";
    private static final String USER_TWO_EMAIL = "test-user-two@localhost";
    private static final String USER_THREE_EMAIL = "test-user-three@localhost";

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService domainUserDetailsService;

    public User getUserOne() {
        User userOne = new User();
        userOne.setPassword(RandomStringUtils.randomAlphanumeric(60));
        userOne.setEmail(USER_ONE_EMAIL);
        userOne.setFullname("userOne");
        return userOne;
    }

    public User getUserTwo() {
        User userTwo = new User();
        userTwo.setPassword(RandomStringUtils.randomAlphanumeric(60));
        userTwo.setEmail(USER_TWO_EMAIL);
        userTwo.setFullname("userTwo");
        return userTwo;
    }

    public User getUserThree() {
        User userThree = new User();
        userThree.setPassword(RandomStringUtils.randomAlphanumeric(60));
        userThree.setEmail(USER_THREE_EMAIL);
        userThree.setFullname("userThree");
        return userThree;
    }

    @BeforeEach
    public void init() {
        userAccountRepository.save(getUserOne());
        userAccountRepository.save(getUserTwo());
        userAccountRepository.save(getUserThree());
    }

    @AfterEach
    public void cleanup() {
        userAccountService.deleteUser(USER_ONE_EMAIL);
        userAccountService.deleteUser(USER_TWO_EMAIL);
        userAccountService.deleteUser(USER_THREE_EMAIL);
    }

    @Test
    void assertThatUserCanBeFoundByEmail() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_EMAIL);
    }

    @Test
    void assertThatUserCanBeFoundByEmailIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_EMAIL);
    }
}
