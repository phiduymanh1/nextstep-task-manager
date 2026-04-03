package org.example.nextstepbackend.dto.request;

import java.time.LocalDateTime;

public record CommentResponse(
    Integer id, Integer userId, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {}
