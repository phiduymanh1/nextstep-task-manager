package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;

public record UserUpdateRequest(
    @Size(max = 100) String fullName,
    @Pattern(regexp = Const.PHONE_REGEX, message = ValidateMessageConst.PHONE_VALID)
        String phone) {}
