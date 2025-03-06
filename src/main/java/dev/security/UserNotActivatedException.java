package dev.security;

import org.springframework.security.core.AuthenticationException;

/**
 * @author Nelson Tanko
 */
public class UserNotActivatedException extends AuthenticationException {
    public UserNotActivatedException(String message) {
        super(message);
    }
}
