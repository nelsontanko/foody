package dev.services.courier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nelson Tanko
 */
public interface CourierRepository extends JpaRepository<Courier, Long> {

    List<Courier> findByActiveTrue();

    @Modifying
    @Query("UPDATE Courier c SET c.available = true WHERE c.available = false AND c.availableFrom < :now")
    void updateAvailabilityStatus(@Param("now") LocalDateTime now);
}
