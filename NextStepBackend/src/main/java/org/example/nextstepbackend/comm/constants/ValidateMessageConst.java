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
}
