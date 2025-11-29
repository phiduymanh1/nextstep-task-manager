package org.example.nextstepbackend.Dto.Response;

import java.time.LocalDateTime;

public record AuditRecord(LocalDateTime createdAt, LocalDateTime updatedAt) {
}
