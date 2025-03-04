package dev.services.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Nelson Tanko
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r WHERE r.active = true ORDER BY r.available DESC")
    Page<Restaurant> findByActiveTrueOrderByAvailableDesc(Pageable pageable);

    List<Restaurant> findByAvailableAndActive(boolean available, boolean active);

    boolean existsByName(String tastyBites);
}
