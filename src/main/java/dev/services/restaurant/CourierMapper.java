package dev.services.restaurant;

import dev.services.restaurant.CourierDTO.Request;
import dev.services.restaurant.CourierDTO.Response;
import dev.services.restaurant.CourierDTO.UpdateRequest;
import org.mapstruct.*;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourierMapper {

    @Mapping(target = "id", ignore = true)
    Courier toEntity(Request request);

    Courier toEntityUpdate(UpdateRequest request);

    Response toResponseDto(Courier courier);

    @Mapping(target = "available", ignore = true)
    void updateCourierFromDto(UpdateRequest request, @MappingTarget Courier courier);
}