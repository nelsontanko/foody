package dev.account.user;

import dev.account.dto.AdminUserDTO;
import dev.account.dto.UserUpdateDTO;
import dev.account.mapper.UserMapper;
import dev.account.web.errors.AccountResourceException;
import dev.account.web.errors.EmailAlreadyUsedException;
import dev.account.web.errors.InvalidPasswordException;
import dev.core.utils.RandomUtils;
import dev.security.AuthoritiesConstants;
import dev.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nelson Tanko
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserAccountService {

    public final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

    private final UserAccountRepository userAccountRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;
    private final UserMapper userMapper;

    public UserAccountService(UserAccountRepository userAccountRepository, AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder, CacheManager cacheManager, UserMapper userMapper) {
        this.userAccountRepository = userAccountRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
        this.userMapper = userMapper;
    }

    public void register(AdminUserDTO userDTO, String password){
        User newUser = createUser(userDTO, UserCreateConfig.builder()
                .password(password)
                .build());
        assignAuthorities(newUser);

        userAccountRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("User Creation successful: {}", newUser.getEmail());
    }

    public void createUser(AdminUserDTO userDTO) {
        User newUser = createUser(userDTO, UserCreateConfig.builder()
                .password(RandomUtils.generatePassword())
                .build());

        if (userDTO.getAuthorities() != null) {
            assignAuthorities(newUser, userDTO.getAuthorities());
        }
        userAccountRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
    }

    public void updateUser(String currentUserEmail, UserUpdateDTO userUpdateDTO) {
        LOG.debug("Request to update User: {}", currentUserEmail);

        User user = userAccountRepository.findOneByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new AccountResourceException("User could not be found"));

        updateUserFields(user, userUpdateDTO);

        userAccountRepository.save(user);
        this.clearUserCaches(user);
        LOG.debug("Changed Information for User: {}", user);
    }

    public void deleteUser(String email) {
        userAccountRepository
                .findOneByEmailIgnoreCase(email)
                .ifPresent(user -> {
                    userAccountRepository.delete(user);
                    this.clearUserCaches(user);
                    LOG.debug("Deleted User: {}", user);
                });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUser()
                .flatMap(userAccountRepository::findOneByEmailIgnoreCase)
                .ifPresent(user -> {
                    String currentEncryptedPassword = user.getPassword();
                    if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                        throw new InvalidPasswordException();
                    }
                    String encryptedPassword = passwordEncoder.encode(newPassword);
                    user.setPassword(encryptedPassword);
                    this.clearUserCaches(user);
                    LOG.debug("Changed password for User: {}", user);
                });
    }

    public Optional<String> requestPasswordReset(String email) {
        LOG.info("Looking for user with email: {}", email);
        return userAccountRepository
                .findOneByEmailIgnoreCase(email)
                .map(user -> {
                    String resetKey = RandomUtils.generateResetKey();
                    user.setResetKey(resetKey);
                    user.setResetDate(LocalDateTime.now());
                    this.clearUserCaches(user);
                    userAccountRepository.save(user);
                    return resetKey;
                });
    }

    public String completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);

        Optional<User> userOptional = userAccountRepository.findOneByResetKey(key);

        if (userOptional.isEmpty()) {
            LOG.warn("Password reset failed: Invalid reset key {}", key);
            return "Invalid reset key";
        }

        User user = userOptional.get();
        if (user.getResetDate() == null || user.getResetDate().isBefore(LocalDateTime.now().minusHours(10))) {
            LOG.warn("Password reset failed: Reset key {} has expired", key);
            return "Reset key has expired";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetKey(null);
        user.setResetDate(null);
        userAccountRepository.save(user);
        this.clearUserCaches(user);

        LOG.info("Password reset successful for user {}", user.getEmail());
        return "Password reset successful";
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUser()
                .flatMap(userAccountRepository::findOneWithAuthoritiesByEmailIgnoreCase);
    }

    @Scheduled(cron = "0 0 */10 * * *") // Runs every 10 hours
    public void removeExpiredResetKeys() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(10);
        int deletedCount = userAccountRepository.removeExpiredResetKeys(expirationTime);
        LOG.debug("Cleanup executed: {} reset keys removed.", deletedCount);
    }

    private User createUser(AdminUserDTO userDTO, UserCreateConfig config) {
        validateExistingUser(userDTO);

        return UserBuilder.builder()
                .withBaseUser(userMapper.toUser(userDTO))
                .withEmail(userDTO.getEmail().toLowerCase())
                .withPassword(passwordEncoder.encode(config.getPassword()))
                .build();
    }

    private void updateUserFields(User user, UserUpdateDTO userUpdateDTO) {
        Optional.ofNullable(userUpdateDTO.getFullname())
                .filter(StringUtils::hasText)
                .ifPresent(user::setFullname);
    }

    private void assignAuthorities(User user, Set<String> authorityStrings) {
        Set<Authority> authorities = authorityStrings.stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        user.setAuthorities(authorities);
    }

    private void assignAuthorities(User user) {
        Set<Authority> authorities = getRole(AuthoritiesConstants.USER);
        user.setAuthorities(authorities);
    }

    private Set<Authority> getRole(String role) {
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(role).ifPresent(authorities::add);
        return authorities;
    }

    private void validateExistingUser(AdminUserDTO userDTO) {
        userAccountRepository.findOneByEmailIgnoreCase(userDTO.getEmail())
                .ifPresent(existingUser -> {
                    throw new EmailAlreadyUsedException();
                });
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserAccountRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserAccountRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }
}
