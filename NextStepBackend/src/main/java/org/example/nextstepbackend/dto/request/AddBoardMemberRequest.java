package org.example.nextstepbackend.dto.request;

import org.example.nextstepbackend.enums.BoardRole;

public record AddBoardMemberRequest(Integer userId, BoardRole role) {}
