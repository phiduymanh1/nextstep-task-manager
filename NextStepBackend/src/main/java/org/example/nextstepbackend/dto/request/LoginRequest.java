package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;

public record LoginRequest(
    @NotBlank(message = ValidateMessageConst.EMAIL_REQUIRED)
        @Email(message = ValidateMessageConst.EMAIL_VALID)
        String email,
    @NotBlank(message = ValidateMessageConst.PASSWORD_REQUIRED) String password) {}
