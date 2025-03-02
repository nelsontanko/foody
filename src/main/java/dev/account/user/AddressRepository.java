package dev.account.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Nelson Tanko
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    boolean existsByLatitudeAndLongitude(Double latitude, Double longitude);
}
