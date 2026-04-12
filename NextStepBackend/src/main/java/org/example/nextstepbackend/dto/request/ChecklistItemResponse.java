package org.example.nextstepbackend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChecklistItemResponse(
    Integer id,
    String content,
    Boolean isCompleted,
    Integer completedBy,
    LocalDateTime completedAt,
    BigDecimal position,
    LocalDateTime dueDate) {}
