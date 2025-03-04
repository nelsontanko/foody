package dev.account.mapper;

import dev.account.dto.AdminUserDTO;
import dev.account.dto.UserDTO;
import dev.account.user.Authority;
import dev.account.user.User;
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
    void updateUserFromDTO(AdminUserDTO userDTO, @MappingTarget User user);

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
}
