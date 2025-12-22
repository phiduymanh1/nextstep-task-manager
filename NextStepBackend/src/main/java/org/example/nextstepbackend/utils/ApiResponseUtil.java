package org.example.nextstepbackend.utils;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.response.ApiResponse;
import org.example.nextstepbackend.dto.response.ResponseMetaData;
import org.example.nextstepbackend.enums.MessageConst;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiResponseUtil {

  private final MessageSource messageSource;

  // Success with code + message
  public <T> ApiResponse<T> success(MessageConst msConst, T data) {

    return new ApiResponse<>(
        new ResponseMetaData(
            true,
            msConst.getCode(),
            messageSource.getMessage(msConst.getCode(), null, LocaleContextHolder.getLocale()),
            LocalDateTime.now(),
            List.of()),
        data);
  }

  // Error 1 message with code
  public ApiResponse<?> error(MessageConst mesConst) {
    String msg =
        messageSource.getMessage(mesConst.getCode(), null, LocaleContextHolder.getLocale());
    return new ApiResponse<>(
        new ResponseMetaData(false, mesConst.getCode(), msg, LocalDateTime.now(), List.of(msg)),
        null);
  }

  public ApiResponse<?> error(String msCode, String msg) {
    return new ApiResponse<>(
        new ResponseMetaData(false, msCode, msg, LocalDateTime.now(), List.of(msg)), null);
  }

  // Error multi message with code
  public ApiResponse<?> error(String code, String message, List<String> errors) {
    return new ApiResponse<>(
        new ResponseMetaData(false, code, message, LocalDateTime.now(), errors), null);
  }

  // Validation error list
  public ApiResponse<?> validationError(List<String> errors) {
    return new ApiResponse<>(
        new ResponseMetaData(
            false, "VALIDATION_ERROR", "Validation failed", LocalDateTime.now(), errors),
        null);
  }
}
