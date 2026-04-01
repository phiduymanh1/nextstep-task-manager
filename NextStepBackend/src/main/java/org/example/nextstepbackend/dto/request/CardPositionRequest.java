package org.example.nextstepbackend.dto.request;

public record CardPositionRequest(
    @jakarta.validation.constraints.NotNull(message = "listId is required") Integer listId,
    Integer afterId,
    Integer beforeId) {}
