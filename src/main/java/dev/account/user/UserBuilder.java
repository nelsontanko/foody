package dev.account.user;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
public class UserBuilder {
    private User user;

    private UserBuilder() {
        this.user = new User();
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public UserBuilder withBaseUser(User baseUser) {
        this.user = baseUser;
        return this;
    }

    public UserBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }

    public UserBuilder withPassword(String password) {
        user.setPassword(password);
        return this;
    }

    public UserBuilder withActivationDetails(boolean activated, String activationKey) {
        user.setActivated(activated);
        user.setActivationKey(activationKey);
        return this;
    }

    public User build() {
        return user;
    }
}