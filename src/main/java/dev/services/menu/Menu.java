package dev.services.menu;

import dev.account.AbstractAuditingEntity;
import dev.services.comment.Comment;
import dev.services.order.OrderItem;
import dev.services.rating.Rating;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@Table(name = "menus")
public class Menu extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_sequence")
    @SequenceGenerator(name = "menu_sequence", sequenceName = "menu_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
}
