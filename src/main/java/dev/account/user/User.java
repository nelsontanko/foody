package dev.account.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.core.common.AbstractAuditingEntity;
import dev.core.validation.ValidEmail;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Nelson Tanko
 * @see AbstractAuditingEntity
 */
@Entity
@Builder
@Getter @Setter
@AllArgsConstructor
@Table(name = "users")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
    @SequenceGenerator(name = "user_sequence", sequenceName = "user_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotNull @JsonIgnore
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Column(length = 50)
    private String fullname;

    @ValidEmail
    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Size(max = 20)
    @JsonIgnore
    private String resetKey;

    private LocalDateTime resetDate;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Address> addresses = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")}
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.status = UserStatus.ACTIVE;
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(password, user.password) && Objects.equals(fullname, user.fullname) && Objects.equals(email, user.email) && Objects.equals(resetKey, user.resetKey) && Objects.equals(resetDate, user.resetDate) && status == user.status && Objects.equals(addresses, user.addresses) && Objects.equals(authorities, user.authorities);
    }

    @Override public int hashCode() {
        return Objects.hash(id, password, fullname, email, resetKey, resetDate, status, addresses, authorities);
    }

    @Override public String toString() {
        return "User{" +
                "id=" + id +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", resetKey='" + resetKey + '\'' +
                ", resetDate=" + resetDate +
                ", status=" + status +
                ", addresses=" + addresses +
                ", authorities=" + authorities +
                '}';
    }
}
