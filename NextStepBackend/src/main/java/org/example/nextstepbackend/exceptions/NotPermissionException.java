package org.example.nextstepbackend.exceptions;

import org.springframework.http.HttpStatus;

public class NotPermissionException extends AppException {

  public NotPermissionException(String message) {
    super(HttpStatus.FORBIDDEN, message, "AUTH_ERROR");
  }
}
