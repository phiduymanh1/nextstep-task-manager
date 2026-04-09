package org.example.nextstepbackend.dto.request;

import org.example.nextstepbackend.enums.BoardRole;

public record BoardMemberResponse(
        Integer userId,
        String fullName,
        String avatarUrl,
        BoardRole role
) {}
