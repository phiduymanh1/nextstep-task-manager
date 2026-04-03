package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.CommentResponse;
import org.example.nextstepbackend.entity.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  CommentResponse toResponse(Comment comment);

  List<CommentResponse> toList(List<Comment> comments);
}
