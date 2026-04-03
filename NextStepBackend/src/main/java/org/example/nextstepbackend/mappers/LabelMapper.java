package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.BoardLabelRequest;
import org.example.nextstepbackend.dto.request.LabelResponse;
import org.example.nextstepbackend.entity.Label;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LabelMapper {

  LabelResponse toResponse(Label label);

  List<LabelResponse> toList(List<Label> labels);

  Label toEntity(BoardLabelRequest request);
}
