package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.enums.Visibility;

public record WorkSpaceUpdateRequest(
    @NotBlank(message = ValidateMessageConst.NAME_REQUIRED)
        @Size(min = 4, max = 20, message = ValidateMessageConst.NAME_INVALID_SIZE)
        String name,
    String description,
    Visibility visibility) {}
