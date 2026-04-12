package org.example.nextstepbackend.dto.response.card;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardResponse(
    Integer id,
    String title,
    String description,
    BigDecimal position,
    Boolean isCompleted,
    Boolean isArchived,
    LocalDateTime dueDate) {}
