package dev.account.web;

import dev.account.dto.*;
import dev.account.user.UserAccountService;
import dev.account.web.errors.AccountResourceException;
import dev.account.web.errors.InvalidPasswordException;
import dev.account.web.vm.KeyAndPasswordVM;
import dev.account.web.vm.ManagedUserVM;
import dev.security.SecurityUtils;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

/**
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("/api/account")
public class AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    private final UserAccountService userAccountService;

    public AccountResource(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ManagedUserVM managedUserVM){
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        userAccountService.register(managedUserVM, managedUserVM.getPassword());
        return new ResponseEntity<>(Collections.singletonMap("message", "Registration successful"), HttpStatus.CREATED);
    }

    /**
     * {@code PATCH  /account} : Update the current user information.
     *
     * @param userUpdateDTO the user information to update.
     * @throws AccountResourceException {@code 404 (Not Found)} if the user couldn't be found.
     */
    @PatchMapping
    @PreAuthorize("hasAuthority('ROLE_USER') OR hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserAccount(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        LOG.debug("REST request to update user account: {}", userUpdateDTO);

        String currentUserEmail = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccountResourceException("Current user login not found"));

        userAccountService.updateUser(currentUserEmail, userUpdateDTO);
        return ResponseEntity.ok(Collections.singletonMap("message", "Information updated successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.newPassword())) {
            throw new InvalidPasswordException();
        }
        userAccountService.changePassword(passwordChangeDto.currentPassword(), passwordChangeDto.newPassword());
        return ResponseEntity.ok(Collections.singletonMap("message", "Password changed successfully"));
    }

    @PostMapping(path = "/reset-password/init")
    public ResponseEntity<?> requestPasswordReset(@RequestBody ResetEmailDTO resetEmailDTO) {
        Optional<String> resetKey = userAccountService.requestPasswordReset(resetEmailDTO.email());
        if (resetKey.isPresent()) {
            // This could be where email is sent, but I am not implementing email sending for this
            LOG.info("Password reset key generated for {}", resetEmailDTO.email());
            return ResponseEntity.ok(Collections.singletonMap("resetKey", resetKey.get()));
        } else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            LOG.warn("Password reset requested for non existing email");
            return ResponseEntity.ok("If your email exists, you will receive reset instructions.");
        }
    }

    @PostMapping(path = "/reset-password/finish")
    public ResponseEntity<?> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        String message = userAccountService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getResetKey());

        return ResponseEntity.ok(Collections.singletonMap("message", message));
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping
    @ResponseStatus(OK)
    public AdminUserDTO getAccount() {
        return userAccountService
                .getUserWithAuthorities()
                .map(AdminUserDTO::new)
                .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    @GetMapping("/auth-info")
    public Map<String, Object> getAuthInfo() {
        Map<String, Object> result = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            result.put("authenticated", auth.isAuthenticated());
            result.put("principal", auth.getPrincipal());
            result.put("name", auth.getName());
            result.put("authorities", auth.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet()));
            result.put("details", auth.getDetails());
        } else {
            result.put("authenticated", false);
            result.put("error", "No authentication found");
        }

        LOG.debug("Debug auth info: {}", result);
        return result;
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
                StringUtils.isEmpty(password) ||
                        password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
                        password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }
}
