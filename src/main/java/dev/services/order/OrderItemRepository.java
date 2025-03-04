package dev.services.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Nelson Tanko
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
