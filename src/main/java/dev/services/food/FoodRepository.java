package dev.services.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public interface FoodRepository extends JpaRepository<Food, Long>, JpaSpecificationExecutor<Food> {

    Optional<Food> findByName(String name);
}