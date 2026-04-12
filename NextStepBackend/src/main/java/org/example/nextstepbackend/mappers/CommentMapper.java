package org.example.nextstepbackend.mappers;

import java.util.List;
import org.example.nextstepbackend.dto.request.CommentResponse;
import org.example.nextstepbackend.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userName", source = "user.username")
  @Mapping(target = "avatarUrl", source = "user.avatarUrl")
  CommentResponse toResponse(Comment comment);

  List<CommentResponse> toList(List<Comment> comments);
}
