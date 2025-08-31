package dev.account.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Nelson Tanko
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
