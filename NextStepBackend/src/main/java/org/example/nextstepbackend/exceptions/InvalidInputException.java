package org.example.nextstepbackend.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends AppException {
  public InvalidInputException(String message) {
    super(HttpStatus.BAD_REQUEST, message, "INVALID_INPUT_ERROR");
  }
}
