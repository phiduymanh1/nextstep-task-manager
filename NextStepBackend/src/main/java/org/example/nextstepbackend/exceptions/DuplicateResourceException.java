package org.example.nextstepbackend.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends AppException {
  public DuplicateResourceException(String message) {
    super(HttpStatus.CONFLICT, message, "CONFLICT");
  }
}
