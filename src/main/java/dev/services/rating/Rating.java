package dev.services.rating;

import dev.account.AbstractAuditingEntity;
import dev.account.user.User;
import dev.services.menu.Menu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@AllArgsConstructor
@Table(name = "ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "menu_id"}))
public class Rating extends AbstractAuditingEntity<Long>{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rating_sequence")
    @SequenceGenerator(name = "rating_sequence", sequenceName = "rating_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer rating;
}
