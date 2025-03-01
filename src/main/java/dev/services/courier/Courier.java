package dev.services.courier;

import dev.account.AbstractAuditingEntity;
import dev.core.validation.ValidMobileNumber;
import dev.services.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;

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

    @OneToOne
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private Restaurant restaurant;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;
}
