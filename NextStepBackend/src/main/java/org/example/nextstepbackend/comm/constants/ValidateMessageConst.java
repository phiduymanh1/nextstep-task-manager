package org.example.nextstepbackend.comm.constants;

public class ValidateMessageConst {

  private ValidateMessageConst() {
    throw new IllegalStateException("Utility class");
  }

  public static final String EMAIL_REQUIRED = "Email is required";
  public static final String EMAIL_VALID = "Email should be valid";
  public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String EMAIL_SIZE_MAX = "Email must be at most ";
}
