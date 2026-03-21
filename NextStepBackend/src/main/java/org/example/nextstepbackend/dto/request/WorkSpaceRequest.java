package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.enums.Visibility;

public record WorkSpaceRequest(
    @NotBlank(message = ValidateMessageConst.NAME_REQUIRED)
        @Size(min = 4, max = 20, message = ValidateMessageConst.NAME_INVALID_SIZE)
        String name,
    @Size(max = 255, message = ValidateMessageConst.DESCRIPTION_INVALID_SIZE) String description,
    @NotNull(message = ValidateMessageConst.VISIBILITY_REQUIRED) Visibility visibility) {}
