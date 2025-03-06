package dev.account.web;


import dev.account.dto.AdminUserDTO;
import dev.account.user.User;
import dev.account.user.UserAccountRepository;
import dev.account.user.UserAccountService;
import dev.account.web.errors.EmailAlreadyUsedException;
import dev.core.config.Constants;
import dev.core.exception.BadRequestAlertException;
import dev.core.utils.HeaderUtils;
import dev.core.utils.PaginationUtils;
import dev.core.utils.ResponseUtils;
import dev.security.AuthoritiesConstants;
import dev.services.common.MailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link dev.account.user.User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
public class UserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    "id",
                    "email",
                    "fullname",
                    "activated",
                    "langKey",
                    "createdBy",
                    "createdDate",
                    "lastModifiedBy",
                    "lastModifiedDate"
            )
    );

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Value("${foody.clientApp.name}")
    private String applicationName;

    private final UserAccountService userAccountService;

    private final UserAccountRepository userAccountRepository;

    private final MailService mailService;

    public UserResource(UserAccountService userAccountService, UserAccountRepository userAccountRepository, MailService mailService) {
        this.userAccountService = userAccountService;
        this.userAccountRepository = userAccountRepository;
        this.mailService = mailService;
    }

    /**
     * {@code POST  /admin/users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * email with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user,
     * or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException       if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody AdminUserDTO userDTO) throws URISyntaxException {
        LOG.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "id exists");
        } else if (userAccountRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userAccountService.createUser(userDTO);
            mailService.sendCreationEmail(newUser);
            return ResponseEntity.created(new URI("/api/admin/users/" + newUser.getEmail()))
                    .headers(HeaderUtils.createAlert(applicationName, "userManagement.created", newUser.getEmail()))
                    .body(newUser);
        }
    }

    /**
     * {@code PUT /admin/users} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     */
    @PatchMapping({ "/users", "/users/{email}" })
    public ResponseEntity<AdminUserDTO> updateUser(@PathVariable(name = "email", required = false) String email,
                                                   @Valid @RequestBody AdminUserDTO userDTO
    ) {
        LOG.debug("REST request to update User : {}", userDTO.getEmail());
        Optional<User> existingUser = userAccountRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.orElseThrow().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<AdminUserDTO> updatedUser = userAccountService.updateUser(userDTO);

        return ResponseUtils.wrapOrNotFound(
                updatedUser,
                HeaderUtils.createAlert(applicationName, "userManagement.updated", userDTO.getEmail())
        );
    }

    /**
     * {@code GET /admin/users} : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }
        final Page<AdminUserDTO> page = userAccountService.getAllManagedUsers(pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty)
                .allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    /**
     * {@code GET /admin/users/:login} : get the "login" user.
     *
     * @param email the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user,
     * or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{email}")
    public ResponseEntity<AdminUserDTO> getUser(@PathVariable("email") @Pattern(regexp = Constants.LOGIN_REGEX) String email) {
        LOG.debug("REST request to get User : {}", email);
        return ResponseUtils.wrapOrNotFound(userAccountService.getUserWithAuthoritiesByEmail(email).map(AdminUserDTO::new));
    }

    /**
     * {@code DELETE /admin/users/:login} : delete the "login" User.
     *
     * @param email the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable("email") @Pattern(regexp = Constants.LOGIN_REGEX) String email) {
        LOG.debug("REST request to delete User: {}", email);
        userAccountService.deleteUser(email);
        return ResponseEntity.noContent()
                .headers(HeaderUtils.createAlert(applicationName, "userManagement.deleted", email)).build();
    }
}
