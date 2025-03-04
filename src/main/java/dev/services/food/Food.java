package dev.services.food;

import dev.core.common.AbstractAuditingEntity;
import dev.services.comment.Comment;
import dev.services.rating.Rating;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "food")
public class Food extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "food_sequence")
    @SequenceGenerator(name = "food_sequence", sequenceName = "food_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    private Double averageRating;

    private Integer totalRatings = 0;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "is_available", columnDefinition = "boolean default true")
    private boolean available;

    @PrePersist
    protected void onCreate(){
        this.available = true;
    }

    public void calculateAverageRating() {
        if (ratings.isEmpty()) {
            this.averageRating = 0.0;
            this.totalRatings = 0;
        } else {
            double sum = ratings.stream().mapToInt(Rating::getRating).sum();
            this.averageRating = sum / ratings.size();
            this.totalRatings = ratings.size();
        }
    }
}
