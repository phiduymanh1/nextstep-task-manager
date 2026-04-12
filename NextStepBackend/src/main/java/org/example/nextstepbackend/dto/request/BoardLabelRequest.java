package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BoardLabelRequest(
    @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be <= 50 characters")
        String name,
    @NotBlank(message = "Color is required")
        @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Color must be a valid hex code (e.g. #FF5733)")
        String color) {}
