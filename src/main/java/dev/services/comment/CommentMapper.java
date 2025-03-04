package dev.services.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author Nelson Tanko
 */

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "food.id", target = "foodId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullname", target = "username")
    @Mapping(target = "createdAt", source = "createdDate")
    CommentDTO.Response toDto(Comment comment);

    List<CommentDTO.Response> toDtoList(List<Comment> comments);

    @Mapping(target = "food", ignore = true)
    @Mapping(target = "user", ignore = true)
    Comment toEntity(CommentDTO.Request commentDTO);
}