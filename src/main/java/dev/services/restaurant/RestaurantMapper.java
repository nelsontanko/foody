package dev.services.restaurant;

import dev.account.user.AddressMapper;
import dev.services.courier.CourierMapper;
import dev.services.restaurant.RestaurantDTO.UpdateRequest;
import org.mapstruct.*;

import java.util.List;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring", uses = {AddressMapper.class}, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Restaurant toEntity(RestaurantDTO.Request request);

    RestaurantDTO.Response toResponseDto(Restaurant restaurant);

    @Mapping(target = "courier", source = "courier")
    RestaurantDTO.DetailedResponse toDetailedResponseDto(Restaurant restaurant);

    List<RestaurantDTO.Response> toResponseDtoList(List<Restaurant> restaurants);

    void updateRestaurantFromDto(UpdateRequest request, @MappingTarget Restaurant restaurant);

}
