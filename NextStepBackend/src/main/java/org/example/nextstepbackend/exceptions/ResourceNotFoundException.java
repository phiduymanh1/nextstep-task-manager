package org.example.nextstepbackend.exceptions;

import org.springframework.http.HttpStatus;

/** Exception thrown when a requested resource is not found. */
public class ResourceNotFoundException extends AppException {

  public ResourceNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message, "RESOURCE_NOT_FOUND");
  }
}
