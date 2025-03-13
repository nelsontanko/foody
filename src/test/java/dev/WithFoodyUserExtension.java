package dev;

import dev.account.user.Authority;
import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

/**
 * @author Nelson Tanko
 */
public class WithFoodyUserExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(WithFoodyUserExtension.class);
    private static final String TEST_USER_KEY = "testUser";
    private static final String AUTH_KEY = "originalAuth";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Store the original authentication
        Store store = context.getStore(NAMESPACE);
        store.put(AUTH_KEY, SecurityContextHolder.getContext().getAuthentication());

        // Get the annotation from the test method
        WithFoodyUser annotation = context.getRequiredTestMethod()
                .getAnnotation(WithFoodyUser.class);

        if (annotation != null) {
            String email = annotation.email();

            ApplicationContext appContext = SpringExtension.getApplicationContext(context);

            UserAccountRepository userRepository = appContext.getBean(UserAccountRepository.class);

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword("password123");

            Set<Authority> authorities = new HashSet<>();
            for (String role : annotation.authorities()) {
                Authority authority = new Authority();
                authority.setName(role);
                authorities.add(authority);
            }
            newUser.setAuthorities(authorities);

            User savedUser = userRepository.save(newUser);
            store.put(TEST_USER_KEY, savedUser);


            // Set up security context if authenticated flag is true
            if (annotation.authenticated()) {
                List<SimpleGrantedAuthority> grantedAuthorities = Arrays.stream(annotation.authorities())
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        email, "password123", grantedAuthorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Store store = context.getStore(NAMESPACE);
        User testUser = store.get(TEST_USER_KEY, User.class);

        if (testUser != null) {
            ApplicationContext appContext = SpringExtension.getApplicationContext(context);
            UserAccountRepository userRepository = appContext.getBean(UserAccountRepository.class);

            userRepository.delete(testUser);

            Authentication originalAuth = store.get(AUTH_KEY, Authentication.class);
            SecurityContextHolder.getContext().setAuthentication(originalAuth);
        }
    }

    /**
     * Static helper method to get the test user from the current test context
     */
    public static User getTestUser(ExtensionContext context) {
        Store store = context.getStore(NAMESPACE);
        return store.get(TEST_USER_KEY, User.class);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == User.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getTestUser(extensionContext);
    }
}