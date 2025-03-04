package dev.account.user;

import dev.core.common.AbstractAuditingEntity;
import dev.services.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Nelson Tanko
 */
@Entity
@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addresses")
public class Address extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_sequence")
    @SequenceGenerator(name = "address_sequence", sequenceName = "address_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    private String country;

    private Double latitude;

    private Double longitude;

    // Calculate distance between two locations using Haversine formula
    public double distanceTo(Address other) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id) && Objects.equals(user, address.user) && Objects.equals(street, address.street) && Objects.equals(city, address.city) && Objects.equals(country, address.country) && Objects.equals(latitude, address.latitude) && Objects.equals(longitude, address.longitude);
    }

    @Override public int hashCode() {
        return Objects.hash(id, user, street, city, country, latitude, longitude);
    }
}
