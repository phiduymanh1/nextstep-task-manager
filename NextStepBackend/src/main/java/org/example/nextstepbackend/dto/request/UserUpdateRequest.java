package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 100) String fullName,
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number is invalid") String phone) {}
