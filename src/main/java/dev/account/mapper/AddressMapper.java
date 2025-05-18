package dev.account.mapper;

import dev.account.user.Address;
import dev.account.user.AddressDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressDTO.Request request);

    AddressDTO.Response toDto(Address address);
}
