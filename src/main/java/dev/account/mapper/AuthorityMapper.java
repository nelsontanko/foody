package dev.account.mapper;

import dev.account.user.Authority;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring")
public interface AuthorityMapper {

    @Mapping(target = "name", source = ".")
    Authority toAuthority(String authorityName);

    @InheritInverseConfiguration
    String toAuthorityName(Authority authority);

    Set<Authority> toAuthorities(Set<String> authorityNames);

    Set<String> toAuthorityNames(Set<Authority> authorities);
}
