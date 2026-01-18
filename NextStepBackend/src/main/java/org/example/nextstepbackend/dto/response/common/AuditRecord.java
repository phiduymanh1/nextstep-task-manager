package org.example.nextstepbackend.dto.response.common;

import java.time.LocalDateTime;

public record AuditRecord(LocalDateTime createdAt, LocalDateTime updatedAt) {}
