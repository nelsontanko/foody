package dev.services.restaurant;

import dev.account.user.AddressMapper;
import dev.services.restaurant.RestaurantDTO.UpdateRequest;
import org.mapstruct.*;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring", uses = { AddressMapper.class, CourierMapper.class }, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Restaurant toEntity(RestaurantDTO.Request request);

    @Mapping(target = "courier", source = "courier")
    @Mapping(target = "address", source = "address")
    RestaurantDTO.Response toResponseDto(Restaurant restaurant);

    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableFrom", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "active", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRestaurantFromDto(UpdateRequest request, @MappingTarget Restaurant restaurant);

    default void postProcess(Restaurant restaurant) {
        if (restaurant.getAddress() != null) {
            restaurant.getAddress().setRestaurant(restaurant);
        }

        if (restaurant.getCourier() != null) {
            restaurant.getCourier().setRestaurant(restaurant);
        }
    }
}
