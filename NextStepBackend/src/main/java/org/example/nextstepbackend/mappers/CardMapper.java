package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.CardDetailResponse;
import org.example.nextstepbackend.dto.response.card.CardResponse;
import org.example.nextstepbackend.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

  CardResponse toCardResponse(Card card);

  @Mapping(target = "labels", ignore = true)
  @Mapping(target = "checklists", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  CardDetailResponse toResponseDetail(Card card);
}
