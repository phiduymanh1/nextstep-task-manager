package org.example.nextstepbackend.dto.response.user;

import org.example.nextstepbackend.dto.response.common.AuditRecord;
import org.example.nextstepbackend.enums.UserRole;

public record UserResponse(
    String username,
    String email,
    String fullName,
    String avatarUrl,
    String phone,
    Boolean isActive,
    UserRole role,
    AuditRecord audit) {}
