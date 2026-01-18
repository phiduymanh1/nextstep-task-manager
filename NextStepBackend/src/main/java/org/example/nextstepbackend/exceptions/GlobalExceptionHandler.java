package org.example.nextstepbackend.exceptions;

import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ApiResponseUtil responseUtil;

  // wrong email / password
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(responseUtil.error(MessageConst.AUTH_INVALID_CREDENTIALS));
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(UsernameNotFoundException ex) {
    log.warn("Username not found");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(responseUtil.error(MessageConst.AUTH_INVALID_CREDENTIALS));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied");
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(responseUtil.error(MessageConst.USER_ACCESS_DENIED));
  }

  // ------------------- Validation errors -------------------
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();

    log.warn("Validation errors: {}", errors);

    return ResponseEntity.badRequest()
        .body(responseUtil.error("VALIDATION_ERROR", "Validation failed", errors));
  }

  // ------------------- Custom AppException -------------------
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
    log.warn("AppException [{}]: {}", ex.getErrorCode(), ex.getMessage());
    return ResponseEntity.status(ex.getStatus())
        .body(responseUtil.error(ex.getErrorCode(), ex.getMessage()));
  }

  // ------------------- General / Unknown errors -------------------
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(responseUtil.error(MessageConst.SYSTEM_INTERNAL_ERROR));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    log.warn("Malformed JSON request: {}", ex.getMessage());

    String errorMessage = "Invalid or misformatted JSON data";

    return ResponseEntity.badRequest()
            .body(responseUtil.error("MALFORMED_JSON", errorMessage, List.of(ex.getMostSpecificCause().getMessage())));
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResponse<Void>> handleDisableAccount(DisabledException ex) {
    log.warn("Account disabled: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(responseUtil.error(MessageConst.AUTH_DISABLED_ACCOUNT));
  }
}
