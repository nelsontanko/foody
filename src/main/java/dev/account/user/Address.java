package dev.account.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * @author Nelson Tanko
 */
@Entity
@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

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

    @Column(nullable = false)
    private String state;

    private String postalCode;
    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    private AddressType type;

    private boolean isDefault;

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return isDefault == address.isDefault && Objects.equals(id, address.id) && Objects.equals(user, address.user) && Objects.equals(street, address.street) && Objects.equals(city, address.city) && Objects.equals(state, address.state) && Objects.equals(postalCode, address.postalCode) && Objects.equals(country, address.country) && type == address.type;
    }

    @Override public int hashCode() {
        return Objects.hash(id, user, street, city, state, postalCode, country, type, isDefault);
    }
}
