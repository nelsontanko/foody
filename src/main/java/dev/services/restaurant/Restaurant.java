package dev.services.restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.account.user.Address;
import dev.core.common.AbstractAuditingEntity;
import dev.core.validation.ValidEmail;
import dev.services.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nelson Tanko
 */
@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "restaurants")
public class Restaurant extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restaurant_sequence")
    @SequenceGenerator(name = "restaurant_sequence", sequenceName = "restaurant_sequence", allocationSize = 1, initialValue = 50)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ValidEmail
    private String email;

    private String phoneNumber;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private boolean active;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private Courier courier;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant")
    private Set<Order> orders = new HashSet<>();

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
