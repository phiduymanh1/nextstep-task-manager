package org.example.nextstepbackend.dto.request;

import org.example.nextstepbackend.enums.WorkspaceRole;

public record WorkspaceMemberResponse(
    Integer userId, String fullName, String avatarUrl, WorkspaceRole role) {}
