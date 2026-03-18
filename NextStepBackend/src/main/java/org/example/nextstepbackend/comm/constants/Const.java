package org.example.nextstepbackend.comm.constants;

public class Const {
  private Const() {
    throw new IllegalStateException("Utility class");
  }

  /** Date time format */
  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /** Regex patterns */
  public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  public static final String PHONE_REGEX = "^0[35789]\\d{8}$";

  /** Text constants */
  public static final String TEXT_REFRESH_TOKEN = "refreshToken";

  public static final String TEXT_ACCESS_TOKEN = "accessToken";

  public static final String BLANK = "";
  public static final String HYPHEN = "-";
}
