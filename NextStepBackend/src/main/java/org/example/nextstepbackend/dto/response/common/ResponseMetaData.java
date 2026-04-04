package org.example.nextstepbackend.dto.response.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ResponseMetaData(
    boolean success,
    String code,
    String message,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp,
    List<String> errors) {
  public ResponseMetaData(boolean success, String code, String message) {
    this(success, code, message, LocalDateTime.now(), List.of());
  }
}
