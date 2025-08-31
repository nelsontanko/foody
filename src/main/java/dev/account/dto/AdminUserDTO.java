package dev.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.account.user.Authority;
import dev.account.user.User;
import dev.account.user.UserStatus;
import dev.core.validation.ValidEmail;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with authorities.
 *
 * @author Nelson Tanko
 */
@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private Long id;

    private String fullname;

    @ValidEmail
    @NotNull(message = "Email is required")
    private String email;

    private boolean activated;

    @Size(min = 2, max = 10)
    private String langKey;

    private UserStatus status;

    private String createdBy;

    private LocalDateTime createdDate;

    private String lastModifiedBy;

    private LocalDateTime lastModifiedDate;

    private Set<String> authorities;

    public AdminUserDTO(User user) {
        this.id = user.getId();
        this.fullname = user.getFullname();
        this.email = user.getEmail();
        this.activated = user.isActivated();
        this.langKey = user.getLangKey();
        this.status = user.getStatus();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.authorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", activated=" + activated +
                ", langKey='" + langKey + '\'' +
                ", status=" + status +
                ", createdBy='" + createdBy + '\'' +
                ", createdDate=" + createdDate +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", authorities=" + authorities +
                '}';
    }
}
