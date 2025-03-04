package dev.account.dto;

import dev.account.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nelson Tanko
 */
@Data
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String email;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
    }
}
