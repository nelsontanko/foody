package dev.services.courier;

import dev.services.courier.CourierDTO.Request;
import dev.services.courier.CourierDTO.Response;
import dev.services.courier.CourierDTO.UpdateRequest;
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

    void updateCourierFromDto(UpdateRequest request, @MappingTarget Courier courier);
}