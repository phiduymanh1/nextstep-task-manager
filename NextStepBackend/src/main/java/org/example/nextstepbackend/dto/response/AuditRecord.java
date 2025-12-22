package org.example.nextstepbackend.dto.response;

import java.time.LocalDateTime;

public record AuditRecord(LocalDateTime createdAt, LocalDateTime updatedAt) {}
