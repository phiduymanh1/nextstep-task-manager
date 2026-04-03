package org.example.nextstepbackend.dto.request;

import java.time.LocalDateTime;

public record ActivityResponse(
    Integer id,
    Integer userId,
    String actionType,
    String entityType,
    Integer entityId,
    String message,
    LocalDateTime createdAt) {}
