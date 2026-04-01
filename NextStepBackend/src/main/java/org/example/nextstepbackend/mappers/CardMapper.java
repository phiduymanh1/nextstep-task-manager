package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.response.card.CardResponse;
import org.example.nextstepbackend.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

  CardResponse toCardResponse(Card card);
}
