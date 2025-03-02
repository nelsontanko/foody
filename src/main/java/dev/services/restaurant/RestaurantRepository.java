package dev.services.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nelson Tanko
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByActiveTrue();

    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.available = true")
    List<Restaurant> findAvailableRestaurants();

    List<Restaurant> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Restaurant r SET r.available = true WHERE r.available = false AND r.availableFrom < :now")
    void updateAvailabilityStatus(@Param("now") LocalDateTime now);
}
