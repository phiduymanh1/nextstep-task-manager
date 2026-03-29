package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.dto.response.board.BoardResponse;
import org.example.nextstepbackend.entity.Board;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardMapper {

  /** Convert Board entity to BoardResponse DTO */
  BoardResponse toResponse(Board board);

  Board toEntity(BoardRequest request);
}
