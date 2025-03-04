package dev.services.restaurant;

import dev.core.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "couriers")
public class Courier extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "courier_sequence")
    @SequenceGenerator(name = "courier_sequence", sequenceName = "courier_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @PrePersist
    protected void onCreate() {
        this.available = true;
        this.active = true;
    }

    public void markAsBusy() {
        this.available = false;
        this.availableFrom = LocalDateTime.now().plusMinutes(15);
    }

    public void markAsAvailable() {
        if (!available && availableFrom != null && LocalDateTime.now().isAfter(availableFrom)) {
            this.available = true;
            this.availableFrom = null;
        }
    }
}
