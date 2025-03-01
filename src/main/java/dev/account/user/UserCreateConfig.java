package dev.account.user;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Nelson Tanko
 */
@Builder
@Getter
class UserCreateConfig {
    private String password;
    private boolean generateResetKey;
}