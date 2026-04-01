package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CardRequest(
    @NotBlank(message = "Title is required") @Size(max = 255, message = "Title max 255 chars")
        String title,
    @Size(max = 5000, message = "Description too long") String description,
    Integer afterId,
    Integer beforeId) {}
