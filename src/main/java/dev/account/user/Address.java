package dev.account.user;

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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addresses")
public class Address implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_sequence")
    @SequenceGenerator(name = "address_sequence", sequenceName = "address_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    private String country;

    private Double latitude;

    private Double longitude;

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id) && Objects.equals(user, address.user) && Objects.equals(street, address.street) && Objects.equals(city, address.city) && Objects.equals(country, address.country) && Objects.equals(latitude, address.latitude) && Objects.equals(longitude, address.longitude);
    }

    @Override public int hashCode() {
        return Objects.hash(id, user, street, city, country, latitude, longitude);
    }
}
