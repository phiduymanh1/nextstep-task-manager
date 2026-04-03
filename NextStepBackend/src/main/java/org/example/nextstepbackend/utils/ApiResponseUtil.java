package org.example.nextstepbackend.utils;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.dto.response.common.ResponseMetaData;
import org.example.nextstepbackend.enums.MessageConst;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiResponseUtil {

  private final MessageSource messageSource;

  // Success with data and optional message
  public <T> ApiResponse<T> success(MessageConst msConst, T data) {

    boolean hasMessage = msConst != null;

    return new ApiResponse<>(
        new ResponseMetaData(
            true,
            hasMessage ? msConst.getCode() : null,
            hasMessage
                ? messageSource.getMessage(msConst.getCode(), null, LocaleContextHolder.getLocale())
                : null,
            LocalDateTime.now(),
            List.of()),
        data);
  }

  // Error 1 message with code
  public ApiResponse<Void> error(MessageConst mesConst) {
    String msg =
        messageSource.getMessage(mesConst.getCode(), null, LocaleContextHolder.getLocale());
    return new ApiResponse<>(
        new ResponseMetaData(false, mesConst.getCode(), msg, LocalDateTime.now(), List.of(msg)),
        null);
  }

  // Error 1 message with custom code
  public ApiResponse<Void> error(String msCode, String msg) {
    return new ApiResponse<>(
        new ResponseMetaData(false, msCode, msg, LocalDateTime.now(), List.of(msg)), null);
  }

  // Error multi message with code
  public ApiResponse<Void> error(String code, String message, List<String> errors) {
    return new ApiResponse<>(
        new ResponseMetaData(false, code, message, LocalDateTime.now(), errors), null);
  }

  // Validation error list
  public ApiResponse<Void> validationError(List<String> errors) {
    return new ApiResponse<>(
        new ResponseMetaData(
            false,
            ValidateMessageConst.VALIDATE_CODE,
            "Validation failed",
            LocalDateTime.now(),
            errors),
        null);
  }
}
