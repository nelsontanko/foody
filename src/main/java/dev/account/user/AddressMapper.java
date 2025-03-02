package dev.account.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressDTO.Request addressCreateDto);

    AddressDTO.Response toDto(Address address);
}
