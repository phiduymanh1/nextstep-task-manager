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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ApiResponseUtil responseUtil;

  /** Handle authentication failures and return 401 Unauthorized. */
  @ExceptionHandler({
    BadCredentialsException.class,
    AuthenticationCredentialsNotFoundException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(Exception ex) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return buildError(HttpStatus.UNAUTHORIZED, MessageConst.AUTH_INVALID_CREDENTIALS);
  }

  /** access denied */
  @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(Exception ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return buildError(HttpStatus.FORBIDDEN, MessageConst.USER_ACCESS_DENIED);
  }

  /** Handle validation errors */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();

    log.warn("Validation errors: {}", errors);

    return ResponseEntity.badRequest().body(responseUtil.validationError(errors));
  }

  /** Handle custom application exceptions */
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
    log.warn("AppException [{}]: {}", ex.getErrorCode(), ex.getMessage());
    return ResponseEntity.status(ex.getStatus())
        .body(responseUtil.error(ex.getErrorCode(), ex.getMessage()));
  }

  /** Handle malformed JSON request */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    log.warn("Malformed JSON request: {}", ex.getMessage());

    return buildError(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_JSON",
        "Invalid or misformatted JSON data",
        List.of(ex.getMostSpecificCause().getMessage()));
  }

  /** Handle disabled account */
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResponse<Void>> handleDisableAccount(DisabledException ex) {
    log.warn("Account disabled: {}", ex.getMessage());
    return buildError(HttpStatus.FORBIDDEN, MessageConst.AUTH_DISABLED_ACCOUNT);
  }

  /** Handle all other exceptions */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Unexpected error", ex);
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, MessageConst.SYSTEM_INTERNAL_ERROR);
  }

  /** Build a standard error response using MessageConst. */
  private ResponseEntity<ApiResponse<Void>> buildError(HttpStatus status, MessageConst message) {
    return ResponseEntity.status(status).body(responseUtil.error(message));
  }

  /** Build a detailed error response with custom code, message, and error list. */
  private ResponseEntity<ApiResponse<Void>> buildError(
      HttpStatus status, String code, String message, List<String> details) {
    return ResponseEntity.status(status).body(responseUtil.error(code, message, details));
  }
}
