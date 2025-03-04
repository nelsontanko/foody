package dev.account.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public interface UserAccountRepository extends JpaRepository<User, Long> {

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByResetKey(String resetKey);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.resetKey = NULL, u.resetDate = NULL WHERE u.resetDate <= :expirationTime")
    int removeExpiredResetKeys(LocalDateTime expirationTime);
}
