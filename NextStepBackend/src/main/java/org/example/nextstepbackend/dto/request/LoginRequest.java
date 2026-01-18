package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;

public record LoginRequest(
    @NotBlank(message = ValidateMessageConst.EMAIL_REQUIRED)
        @Email(message = ValidateMessageConst.EMAIL_VALID)
        @Size(max = 100, message = ValidateMessageConst.EMAIL_SIZE_MAX + "{max}")
        String email,
    @NotBlank(message = ValidateMessageConst.PASSWORD_REQUIRED) String password) {}
