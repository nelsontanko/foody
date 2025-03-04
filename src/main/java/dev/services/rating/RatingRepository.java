package dev.services.rating;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<   Rating> findByFoodIdAndUserId(Long foodId, Long userId);

}
