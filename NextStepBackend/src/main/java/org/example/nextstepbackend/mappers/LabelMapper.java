package org.example.nextstepbackend.mappers;

import java.util.List;
import org.example.nextstepbackend.dto.request.LabelResponse;
import org.example.nextstepbackend.entity.Label;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelMapper {

  LabelResponse toResponse(Label label);

  List<LabelResponse> toList(List<Label> labels);
}
