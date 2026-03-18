package org.example.nextstepbackend.comm.constants;

public class ValidateMessageConst {

  private ValidateMessageConst() {
    throw new IllegalStateException("Utility class");
  }

  public static final String VALIDATE_CODE = "VALIDATION_ERROR";

  public static final String EMAIL_REQUIRED = "Email is required";
  public static final String EMAIL_VALID = "Email should be valid";
  public static final String PHONE_VALID = "Phone number is invalid";
  public static final String PASSWORD_REQUIRED = "Password is required";
  public static final String EMAIL_SIZE_MAX = "Email must be at most ";
  public static final String NAME_REQUIRED = "Name is required";
  public static final String NAME_INVALID_SIZE = "The name must be between {min} and {max} characters long.";
  public static final String DESCRIPTION_INVALID_SIZE = "Description should not exceed {max} characters.";
  public static final String VISIBILITY_REQUIRED = "Visibility is required";
  public static final String SLUG_DUPLICATE = "Slug already exists";
}
