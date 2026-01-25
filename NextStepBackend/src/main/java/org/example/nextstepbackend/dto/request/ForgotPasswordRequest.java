package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;

public record ForgotPasswordRequest(
    @NotBlank(message = ValidateMessageConst.EMAIL_REQUIRED)
        @Email(regexp = Const.REGEX_EMAIL, message = ValidateMessageConst.EMAIL_VALID)
        @Size(max = 100, message = ValidateMessageConst.EMAIL_SIZE_MAX + "{max}")
        String email) {}
