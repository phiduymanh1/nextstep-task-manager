package org.example.nextstepbackend.dto.request;

import java.time.LocalDateTime;

public record CardUpdateRequest(
    @jakarta.validation.constraints.Size(max = 255) String title,
    @jakarta.validation.constraints.Size(max = 5000) String description,
    LocalDateTime dueDate,
    Boolean isCompleted,
    @jakarta.validation.constraints.Size(max = 20) String coverColor,
    @jakarta.validation.constraints.Size(max = 500) String coverImageUrl) {}
