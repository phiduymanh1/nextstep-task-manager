package org.example.nextstepbackend.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record ChecklistResponse(
    Integer id, String title, BigDecimal position, List<ChecklistItemResponse> items) {}
