package org.example.nextstepbackend.dto.response.lists;

import java.math.BigDecimal;
import java.util.List;
import org.example.nextstepbackend.dto.response.card.CardResponse;

public record ListDetailResponse(
    Integer id, String name, BigDecimal position, List<CardResponse> cards) {}
