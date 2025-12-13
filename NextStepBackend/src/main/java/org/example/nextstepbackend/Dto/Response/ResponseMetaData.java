package org.example.nextstepbackend.Dto.Response;

import java.time.LocalDateTime;
import java.util.List;

public record ResponseMetaData(
        boolean success,
        String code,
        String message,
        LocalDateTime timestamp,
        List<String> errors
) {
    public ResponseMetaData(boolean success, String code, String message) {
        this(success, code, message, LocalDateTime.now(), List.of());
    }
}
