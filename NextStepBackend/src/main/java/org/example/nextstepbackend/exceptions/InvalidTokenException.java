package org.example.nextstepbackend.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AppException {

  public InvalidTokenException(String message) {
    super(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", message);
  }
}
