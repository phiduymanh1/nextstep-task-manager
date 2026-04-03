package org.example.nextstepbackend.dto.response.lists;

import org.example.nextstepbackend.dto.response.card.CardResponse;

import java.math.BigDecimal;
import java.util.List;

public record ListDetailResponse(
    Integer id, String name, BigDecimal position, List<CardResponse> cards) {}
