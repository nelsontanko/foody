package dev.services.comment;

import dev.account.AbstractAuditingEntity;
import dev.account.user.User;
import dev.services.menu.Menu;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@Table(name = "comments")
public class Comment extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_sequence")
    @SequenceGenerator(name = "comment_sequence", sequenceName = "comment_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
