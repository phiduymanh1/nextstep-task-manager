package org.example.nextstepbackend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChecklistItemRequest(
    String content,
    BigDecimal position,
    LocalDateTime dueDate,
    Integer afterId,
    Integer beforeId
) {}
