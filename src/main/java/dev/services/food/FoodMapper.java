package dev.services.food;

import dev.services.comment.CommentMapper;
import org.mapstruct.*;

import java.util.List;

/**
 * @author Nelson Tanko
 */

@Mapper(componentModel = "spring", uses = { CommentMapper.class })
public interface FoodMapper {

    Food toEntity(FoodDTO.Request foodDto);

    FoodDTO.Response toDto(Food food);

    List<FoodDTO.Response> toDtoList(List<Food> foods);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalRatings", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "available", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFoodFromDto(FoodDTO.UpdateRequest foodDto, @MappingTarget Food food);

}
