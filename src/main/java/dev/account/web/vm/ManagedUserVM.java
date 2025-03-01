package dev.account.web.vm;

import dev.account.dto.AdminUserDTO;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Nelson Tanko
 */

@NoArgsConstructor
@Getter @Setter
public class ManagedUserVM extends AdminUserDTO {

    public static final int PASSWORD_MIN_LENGTH = 8;

    public static final int PASSWORD_MAX_LENGTH = 70;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    @Override
    public String toString() {
        return "ManagedUserVM{" + super.toString() + "} ";
    }
}
