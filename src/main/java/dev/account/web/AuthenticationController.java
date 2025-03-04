package dev.account.web;

import dev.account.dto.JWTTokenDTO;
import dev.account.user.AuthenticationService;
import dev.account.web.vm.LoginVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * REST controller for authentication
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("/api/account")
public class AuthenticationController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticates a user and returns a JWT token
     *
     * @param loginVM login credentials and preferences
     * @return JWT token wrapped in a response entity
     */
    @PostMapping("/authenticate")
    public ResponseEntity<JWTTokenDTO> authorize(@Valid @RequestBody LoginVM loginVM) {
        LOG.debug("REST request to authenticate user: {}", loginVM.getEmail());
        String jwt = authenticationService.authenticate(loginVM.getEmail(), loginVM.getPassword(), loginVM.isRememberMe());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(jwt);
        return new ResponseEntity<>(new JWTTokenDTO("Login successful", jwt), httpHeaders, HttpStatus.OK);
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param principal the authentication principal.
     * @return the login if the user is authenticated.
     */
    @GetMapping(value = "/authenticate", produces = MediaType.TEXT_PLAIN_VALUE)
    public String isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return principal == null ? null : principal.getName();
    }
}