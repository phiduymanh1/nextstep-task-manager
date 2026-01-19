package org.example.nextstepbackend.exceptions;

import org.springframework.http.HttpStatus;

/** Custom exception for authentication errors */
public class AuthException extends AppException {

  public AuthException(String message) {
    super(HttpStatus.UNAUTHORIZED, message, "AUTH_ERROR");
  }
}
