package dev.account.mapper;

import dev.account.dto.AdminUserDTO;
import dev.account.dto.UserDTO;
import dev.account.user.Authority;
import dev.account.user.User;
import dev.core.validation.PhoneNumberConstraintValidator;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    default List<UserDTO> usersToUserDTOs(List<User> users) {
        return users == null ? null : users.stream()
                .filter(Objects::nonNull)
                .map(this::userToUserDTO)
                .toList();
    }

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    default List<User> userDTOsToUsers(List<AdminUserDTO> userDTOs) {
        return userDTOs == null ? null : userDTOs.stream()
                .filter(Objects::nonNull)
                .map(this::toUser)
                .toList();
    }

    List<AdminUserDTO> usersToAdminUserDTOs(List<User> users);

    UserDTO userToUserDTO(User user);

    @Mapping(target = "authorities", source = "authorities", qualifiedByName = "authoritiesToStringSet")
    AdminUserDTO userToAdminUserDTO(User user);

    @Mapping(target = "authorities", source = "authorities", qualifiedByName = "stringSetToAuthorities")
    User toUser(AdminUserDTO userDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "mobileNumber", qualifiedByName = "normalizePhoneNumber")
    void updateUserFromDTO(AdminUserDTO userDTO, @MappingTarget User user);

    @Named("normalizePhoneNumber")
    default String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber != null ? PhoneNumberConstraintValidator.normalize(phoneNumber) : null;
    }

    @Named("authoritiesToStringSet")
    default Set<String> authoritiesToStringSet(Set<Authority> authorities) {
        if (authorities == null) {
            return null;
        }
        return authorities.stream().map(Authority::getName).collect(Collectors.toSet());
    }

    @Named("stringSetToAuthorities")
    default Set<Authority> stringSetToAuthorities(Set<String> authoritiesAsString) {
        if (authoritiesAsString == null) {
            return new HashSet<>();
        }
        return authoritiesAsString.stream().map(name -> {
            Authority authority = new Authority();
            authority.setName(name);
            return authority;
        }).collect(Collectors.toSet());
    }

    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoId(User user);

    @Named("idSet")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Set<UserDTO> toDtoIdSet(Set<User> users);

    @Named("login")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    UserDTO toDtoLogin(User user);

    @Named("loginSet")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    Set<UserDTO> toDtoLoginSet(Set<User> users);

    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
