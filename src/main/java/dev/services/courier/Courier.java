package dev.services.courier;

import dev.account.AbstractAuditingEntity;
import dev.core.validation.ValidMobileNumber;
import dev.services.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@Table(name = "couriers")
public class Courier extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "courier_sequence")
    @SequenceGenerator(name = "courier_sequence", sequenceName = "courier_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ValidMobileNumber
    @Column(length = 15)
    private String phoneNumber;

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
        this.availableFrom = LocalDateTime.now();
    }
    public void setUnavailableForDelivery() {
        this.available = false;
        this.availableFrom = LocalDateTime.now().plusMinutes(15);
    }
}
