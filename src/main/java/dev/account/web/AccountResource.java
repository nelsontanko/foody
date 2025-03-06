package dev.account.web;

import dev.account.dto.*;
import dev.account.user.User;
import dev.account.user.UserAccountService;
import dev.account.web.errors.AccountResourceException;
import dev.account.web.errors.InvalidPasswordException;
import dev.account.web.vm.KeyAndPasswordVM;
import dev.account.web.vm.ManagedUserVM;
import dev.security.SecurityUtils;
import dev.services.common.MailService;
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
    private static final String MESSAGE_KEY = "message";

    private final UserAccountService userAccountService;
    private final MailService mailService;

    public AccountResource(UserAccountService userAccountService, MailService mailService) {
        this.userAccountService = userAccountService;
        this.mailService = mailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ManagedUserVM managedUserVM){
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        User newUser = userAccountService.register(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(newUser);
        return new ResponseEntity<>(Collections.singletonMap(
                MESSAGE_KEY, "Registration successful, please check email to confirm your account"), HttpStatus.CREATED);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @PostMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userAccountService.activateAccount(key);
        if (user.isEmpty()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
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
        return ResponseEntity.ok(Collections.singletonMap(MESSAGE_KEY, "Information updated successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.newPassword())) {
            throw new InvalidPasswordException();
        }
        userAccountService.changePassword(passwordChangeDto.currentPassword(), passwordChangeDto.newPassword());
        return ResponseEntity.ok(Collections.singletonMap(MESSAGE_KEY, "Password changed successfully"));
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param email the mail of the user.
     */
    @PostMapping(path = "/reset-password/init")
    public void requestPasswordReset(@RequestBody String email) {
        Optional<User> user = userAccountService.requestPasswordReset(email);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.orElseThrow());
        } else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            LOG.warn("Password reset requested for non existing email");
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = userAccountService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (user.isEmpty()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
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
