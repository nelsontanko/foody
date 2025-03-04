package dev.services.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Nelson Tanko
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByFoodId(Long foodId, Pageable pageable);

}
