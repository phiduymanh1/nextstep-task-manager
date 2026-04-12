package org.example.nextstepbackend.dto.request;

import java.time.LocalDateTime;
import org.example.nextstepbackend.enums.ActionType;

public record ActivityResponse(
    Integer id,
    Integer userId,
    ActionType actionType,
    String entityType,
    Integer entityId,
    String message,
    LocalDateTime createdAt) {}
