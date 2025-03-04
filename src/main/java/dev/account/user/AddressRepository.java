package dev.account.user;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    boolean existsByLatitudeAndLongitude(Double latitude, Double longitude);

    Optional<Address> findByUserAndStreetAndCityAndAndCountry(User user,
                                      @NotBlank(message = "Street is required") String street,
                                      @NotBlank(message = "City is required") String city, String country);

    List<Address> findByUserOrderByLastModifiedDateDesc(User user);
}
