package dev.services.restaurant;

import dev.account.AbstractAuditingEntity;
import dev.account.user.Address;
import dev.services.courier.Courier;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private Courier courier;

    @OneToMany(mappedBy = "restaurant")
    private Set<Order> orders = new HashSet<>();
}
