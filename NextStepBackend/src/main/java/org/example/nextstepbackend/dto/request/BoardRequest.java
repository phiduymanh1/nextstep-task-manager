package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.enums.Visibility;

public record BoardRequest(
    @NotBlank(message = ValidateMessageConst.NAME_REQUIRED)
        @Size(min = 3, max = 100, message = ValidateMessageConst.NAME_INVALID_SIZE)
        String name,
    String description,
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Invalid color format")
    String backgroundColor,
    @Size(max = 500)
    String backgroundImageUrl,
    Visibility visibility) {}
