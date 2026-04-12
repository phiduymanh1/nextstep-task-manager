package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.ChecklistResponse;
import org.example.nextstepbackend.entity.Checklist;
import org.example.nextstepbackend.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CheckListsMapper {

  ChecklistResponse toResponse(Checklist checklist);

  default Integer map(User user) {
    return user != null ? user.getId() : null;
  }
}
