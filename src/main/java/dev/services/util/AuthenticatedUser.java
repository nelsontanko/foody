package dev.services.util;

import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.security.SecurityUtils;
import org.springframework.stereotype.Component;

/**
 * @author Nelson Tanko
 */
@Component
public class AuthenticatedUser {

    private final UserAccountRepository userAccountRepository;

    public AuthenticatedUser(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public User getAuthenticatedUser() {
        return SecurityUtils.getCurrentUser()
                .flatMap(userAccountRepository::findOneByEmailIgnoreCase)
                .orElseThrow(() -> new GenericApiException(ErrorCode.USER_NOT_FOUND));
    }
}
