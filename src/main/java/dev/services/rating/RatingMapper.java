package dev.services.rating;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(source = "food.id", target = "foodId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullname", target = "username")
    @Mapping(target = "createdAt", source = "createdDate")
    RatingDTO.Response toDto(Rating rating);

    @Mapping(target = "food", ignore = true)
    @Mapping(target = "user", ignore = true)
    Rating toEntity(RatingDTO.Request ratingDTO);
}