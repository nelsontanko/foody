package dev.account.web.vm;

import dev.core.validation.ValidEmail;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Nelson Tanko
 */
@Getter @Setter
public class LoginVM {

    @NotNull
    @ValidEmail
    private String email;

    @NotNull
    private String password;

    private boolean rememberMe;

    @Override public String toString() {
        return "LoginVM{" +
                "email='" + email + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
