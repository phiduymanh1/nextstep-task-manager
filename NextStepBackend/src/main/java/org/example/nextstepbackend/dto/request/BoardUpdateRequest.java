package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.enums.Visibility;

public record BoardUpdateRequest(
    String name,
    String description,
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Invalid color format")
        String backgroundColor,
    @Size(max = 500) String backgroundImageUrl,
    Visibility visibility) {}
