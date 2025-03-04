package dev.account.web;

import dev.account.user.Authority;
import dev.account.user.AuthorityRepository;
import dev.core.exception.BadRequestAlertException;
import dev.core.utils.HeaderUtils;
import dev.core.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * @author Nelson Tanko
 */
@RestController
@Transactional
@RequestMapping("/api/authorities")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
public class AuthorityResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityResource.class);

    private static final String ENTITY_NAME = "adminAuthority";

    @Value("${foody.clientApp.name}")
    private String applicationName;

    private final AuthorityRepository authorityRepository;

    public AuthorityResource(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @PostMapping
    public ResponseEntity<Authority> createAuthority(@Valid @RequestBody Authority authority) throws URISyntaxException {
        LOG.debug("REST request to save Authority : {}", authority);
        if (authorityRepository.existsById(authority.getName())) {
            throw new BadRequestAlertException("authority already exists", ENTITY_NAME, "id exists");
        }
        authority = authorityRepository.save(authority);
        return ResponseEntity.created(new URI("/api/authorities/" + authority.getName()))
                .headers(HeaderUtils.createEntityCreationAlert(applicationName, true, ENTITY_NAME, authority.getName()))
                .body(authority);
    }

    @GetMapping
    public List<Authority> getAllAuthorities() {
        LOG.debug("REST request to get all Authorities");
        return authorityRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Authority> getAuthority(@PathVariable("id") String id) {
        LOG.debug("REST request to get Authority : {}", id);
        Optional<Authority> authority = authorityRepository.findById(id);
        return ResponseUtils.wrapOrNotFound(authority);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthority(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Authority : {}", id);
        authorityRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtils.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
