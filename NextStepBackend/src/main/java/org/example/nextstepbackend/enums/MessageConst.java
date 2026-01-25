package org.example.nextstepbackend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MessageConst {

  // Auth - info messages
  AUTH_REFRESH_SUCCESS("I-AUTH-001", HttpStatus.OK),
  AUTH_REGISTER_SUCCESS("I-AUTH-002", HttpStatus.OK),
  AUTH_FORGOT_PASSWORD_SENT("I-AUTH-003", HttpStatus.OK),
  AUTH_LOGIN_SUCCESS("I-AUTH-004", HttpStatus.OK),
  AUTH_LOGOUT_SUCCESS("I-AUTH-005",HttpStatus.OK),

  // User - info messages
  USER_FETCH_SUCCESS("I-USER-001", HttpStatus.OK),
  USER_UPDATE_SUCCESS("I-USER-002", HttpStatus.OK),
  USER_AVATAR_UPDATE_SUCCESS("I-USER-003", HttpStatus.OK),

  // Auth - error messages
  AUTH_INVALID_CREDENTIALS("E-AUTH-001", HttpStatus.UNAUTHORIZED),
  AUTH_REFRESH_TOKEN_INVALID("E-AUTH-002", HttpStatus.UNAUTHORIZED),
  AUTH_DISABLED_ACCOUNT("E-AUTH-003", HttpStatus.FORBIDDEN),
  AUTH_ERROR("E-AUTH-004", HttpStatus.UNAUTHORIZED),

  // User - error messages
  USER_NOT_FOUND("E-USER-001", HttpStatus.NOT_FOUND),
  USER_ACCESS_DENIED("E-USER-002", HttpStatus.FORBIDDEN),

  // System - error messages
  SYSTEM_INTERNAL_ERROR("E-SYS-001", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final HttpStatus httpStatus;

  MessageConst(String code, HttpStatus httpStatus) {
    this.code = code;
    this.httpStatus = httpStatus;
  }
}
