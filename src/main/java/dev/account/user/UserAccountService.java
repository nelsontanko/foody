package dev.account.user;

import dev.account.dto.AdminUserDTO;
import dev.account.dto.UserDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public User register(AdminUserDTO userDTO, String password) {
        User newUser = createUser(userDTO, UserCreateConfig.builder()
                .password(password)
                .activated(false)
                .generateActivationKey(true)
                .build());
        assignAuthorities(newUser);

        userAccountRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("User Creation successful: {}", newUser.getEmail());
        return newUser;
    }

    public Optional<User> activateAccount(String activationKey) {
        LOG.debug("Activating user for activation key {}", activationKey);
        return userAccountRepository.findOneByActivationKey(activationKey)
                .map(user -> {
                    user.setActivated(true);
                    user.setActivationKey(null);
                    user.setStatus(UserStatus.ACTIVE);

                    LOG.debug("Activated user: {}", user);
                    this.clearUserCaches(user);
                    return user;
                });
    }

    public User createUser(AdminUserDTO userDTO) {
        User newUser = createUser(userDTO, UserCreateConfig.builder()
                .password(RandomUtils.generatePassword())
                .activated(true)
                .generateResetKey(true)
                .build());

        if (userDTO.getAuthorities() != null) {
            assignAuthorities(newUser, userDTO.getAuthorities());
        }
        userAccountRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
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

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return userAccountRepository.findById(userDTO.getId())
                .map(user -> {
                    this.clearUserCaches(user);
                    userMapper.updateUserFromDTO(userDTO, user);

                    Set<Authority> managedAuthorities = user.getAuthorities();
                    managedAuthorities.clear();
                    userDTO.getAuthorities()
                            .stream()
                            .map(authorityRepository::findById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(managedAuthorities::add);
                    userAccountRepository.save(user);
                    this.clearUserCaches(user);
                    LOG.debug("Changed Information for user: {}", user);
                    return user;
                })
                .map(AdminUserDTO::new);
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

    public Optional<User> findByEmail(String email) {
        return userAccountRepository.findOneByEmailIgnoreCase(email);
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

    public Optional<User> requestPasswordReset(String email) {
        LOG.info("Looking for user with email: {}", email);
        return userAccountRepository
                .findOneByEmailIgnoreCase(email)
                .filter(User::isActivated)
                .map(user -> {
                    user.setResetKey(RandomUtils.generateResetKey());
                    user.setResetDate(LocalDateTime.now());
                    this.clearUserCaches(user);
                    return user;
                });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userAccountRepository
                .findOneByResetKey(key)
                .filter(user -> user.getResetDate().isAfter(LocalDateTime.now().minusDays(1)))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(LocalDateTime.now());
                    this.clearUserCaches(user);
                    return user;
                });
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUser()
                .flatMap(userAccountRepository::findOneWithAuthoritiesByEmailIgnoreCase);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userAccountRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByEmail(String email) {
        return userAccountRepository.findOneWithAuthoritiesByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userAccountRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userAccountRepository
                .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(LocalDateTime.now().minusDays(3))
                .forEach(user -> {
                    LOG.debug("Deleting not activated user {}", user.getEmail());
                    userAccountRepository.delete(user);
                    this.clearUserCaches(user);
                });
    }

    private User createUser(AdminUserDTO userDTO, UserCreateConfig config) {
        validateExistingUser(userDTO);

        return UserBuilder.builder()
                .withBaseUser(userMapper.toUser(userDTO))
                .withEmail(userDTO.getEmail().toLowerCase())
                .withPassword(passwordEncoder.encode(config.getPassword()))
                .withActivationDetails(config.isActivated(), config.isGenerateActivationKey() ?
                        RandomUtils.generateActivationKey() : null)
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
