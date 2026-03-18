package org.example.nextstepbackend.dto.response.Workspace;

import org.example.nextstepbackend.entity.embedded.FullAudit;
import org.example.nextstepbackend.enums.Visibility;

public record WorkspaceResponse(
    Integer id,
    String name,
    String slug,
    String description,
    Visibility visibility,
    String createdById,
    String createdByName,
    FullAudit audit) {}
