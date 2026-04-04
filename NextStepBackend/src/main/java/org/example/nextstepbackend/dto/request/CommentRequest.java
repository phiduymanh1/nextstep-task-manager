package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(

        @NotBlank(message = "Content không được để trống")
        @Size(max = 1000, message = "Content tối đa 1000 ký tự")
        String content

) {}
