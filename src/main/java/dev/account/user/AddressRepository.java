package dev.account.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.street = :street AND a.city = :city " +
            "AND a.state = :state AND a.postalCode = :postalCode AND a.country = :country")
    Optional<Address> findByUserAndDetails(User user, String street, String city, String state, String postalCode, String country);
}
