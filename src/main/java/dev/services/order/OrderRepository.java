package dev.services.order;

import dev.account.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Nelson Tanko
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Page<Order> findAllByUserOrderByOrderTimeDesc(User user, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);
}