package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.ListsRequest;
import org.example.nextstepbackend.entity.ListEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListMapper {

  ListEntity toEntity(ListsRequest request);
}
