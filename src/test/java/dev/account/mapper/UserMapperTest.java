package dev.account.mapper;

import dev.account.dto.AdminUserDTO;
import dev.account.dto.UserDTO;
import dev.account.user.Authority;
import dev.account.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    private User user;
    private AdminUserDTO userDto;

    private static final String DEFAULT_EMAIL = "johndoe@example.com";


    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);

        user = new User();
        user.setId(1L);
        user.setFullname("John Doe");
        user.setEmail("john.doe@example.com");

        userDto = new AdminUserDTO();
        userDto.setId(1L);
        userDto.setFullname("John Doe");
        userDto.setEmail("john.doe@example.com");

        Set<String> authorities = new HashSet<>();
        authorities.add("ROLE_USER");
        userDto.setAuthorities(authorities);
    }

    @Test
    void usersToUserDTOsShouldMapOnlyNonNullUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(null);

        List<UserDTO> userDTOS = userMapper.usersToUserDTOs(users);

        assertThat(userDTOS).isNotEmpty();
        assertThat(userDTOS.size()).isEqualTo(1);
    }

    @Test
    void userDTOsToUsersShouldMapOnlyNonNullUsers() {
        List<AdminUserDTO> usersDto = new ArrayList<>();
        usersDto.add(userDto);
        usersDto.add(null);  // Add a null AdminUserDTO

        List<User> users = userMapper.userDTOsToUsers(usersDto);

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(1);  // Only non-null userDto should be mapped
    }

    @Test
    void userDTOsToUsersWithAuthoritiesStringShouldMapToUsersWithAuthoritiesDomain() {
        Set<String> authoritiesAsString = new HashSet<>();
        authoritiesAsString.add("ADMIN");
        userDto.setAuthorities(authoritiesAsString);

        List<AdminUserDTO> usersDto = new ArrayList<>();
        usersDto.add(userDto);

        List<User> users = userMapper.userDTOsToUsers(usersDto);

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getAuthorities()).isNotNull();
        assertThat(users.get(0).getAuthorities()).isNotEmpty();
        assertThat(users.get(0).getAuthorities().iterator().next().getName()).isEqualTo("ADMIN");
    }

    @Test
    void userDTOsToUsersMapWithNullAuthoritiesStringShouldReturnUserWithEmptyAuthorities() {
        userDto.setAuthorities(null);  // Authorities are null

        List<AdminUserDTO> usersDto = new ArrayList<>();
        usersDto.add(userDto);

        List<User> users = userMapper.userDTOsToUsers(usersDto);

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getAuthorities()).isNotNull();
        assertThat(users.get(0).getAuthorities()).isEmpty();
    }

    @Test
    void userDTOToUserMapWithAuthoritiesStringShouldReturnUserWithAuthorities() {
        User convertedUser = userMapper.toUser(userDto);

        assertThat(convertedUser).isNotNull();
        assertThat(convertedUser.getAuthorities()).isNotNull();
        assertThat(convertedUser.getAuthorities()).isNotEmpty();
        assertThat(convertedUser.getAuthorities().iterator().next().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void userDTOToUserMapWithNullAuthoritiesStringShouldReturnUserWithEmptyAuthorities() {
        userDto.setAuthorities(null);

        User persistUser = userMapper.toUser(userDto);

        assertThat(persistUser).isNotNull();
        assertThat(persistUser.getAuthorities()).isNotNull();
        assertThat(persistUser.getAuthorities()).isEmpty();
    }

    @Test
    void userDTOToUserMapWithNullUserShouldReturnNull() {
        assertThat(userMapper.toUser(null)).isNull();
    }

    @Test
    void testUserToUserDTO() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setFullname("John Doe");
        user.setEmail(DEFAULT_EMAIL);

        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authority);
        user.setAuthorities(authorities);

        // When
        UserDTO userDTO = userMapper.userToUserDTO(user);

        // Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(user.getId());
        assertThat(userDTO.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testToUser() {
        // Given
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(1L);
        userDTO.setFullname("John Doe");
        userDTO.setEmail(DEFAULT_EMAIL);

        Set<String> authorityNames = new HashSet<>();
        authorityNames.add("ROLE_ADMIN");
        userDTO.setAuthorities(authorityNames);

        // When
        User user = userMapper.toUser(userDTO);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userDTO.getId());
        assertThat(user.getFullname()).isEqualTo(userDTO.getFullname());
        assertThat(user.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().iterator().next().getName()).isEqualTo("ROLE_ADMIN");
    }
}
