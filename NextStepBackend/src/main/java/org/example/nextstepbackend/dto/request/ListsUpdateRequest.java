package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ListsUpdateRequest(
    @NotBlank(message = "Name không được để trống")
        @Size(min = 1, max = 100, message = "Name phải từ 1-100 ký tự")
        String name) {}
