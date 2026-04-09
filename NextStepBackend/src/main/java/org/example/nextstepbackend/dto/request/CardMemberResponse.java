package org.example.nextstepbackend.dto.request;

public record CardMemberResponse(
        Integer id,
        String fullName,
        String avatarUrl
) {}
