package org.example.nextstepbackend.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Custom application exception */
@Getter
public class AppException extends RuntimeException {

  private final HttpStatus status;
  private final String errorCode;

  public AppException(HttpStatus status, String message, String errorCode) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }
}
