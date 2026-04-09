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
  AUTH_LOGOUT_SUCCESS("I-AUTH-005", HttpStatus.OK),

  // User - info messages
  USER_FETCH_SUCCESS("I-USER-001", HttpStatus.OK),
  USER_UPDATE_SUCCESS("I-USER-002", HttpStatus.OK),
  USER_AVATAR_UPDATE_SUCCESS("I-USER-003", HttpStatus.OK),

  // Work space - info messages
  WORK_SPACE_CREATE_SUCCESS("I-WORK-SPACE-001", HttpStatus.OK),
  WORK_SPACE_UPDATE_SUCCESS("I-WORK-SPACE-002", HttpStatus.OK),
  WORK_SPACE_DELETE_SUCCESS("I-WORK-SPACE-003", HttpStatus.OK),

  // Board - info messages
  BOARD_CREATE_SUCCESS("I-BOARD-001", HttpStatus.OK),
  BOARD_DELETE_SUCCESS("I-BOARD-002", HttpStatus.OK),
  BOARD_UPDATE_SUCCESS("I-BOARD-003", HttpStatus.OK),

  // List - info messages
  LIST_CREATE_SUCCESS("I-LIST-001", HttpStatus.OK),
  LIST_ARCHIVE_SUCCESS("I-LIST-002", HttpStatus.OK),
  LIST_UPDATE_SUCCESS("I-LIST-003", HttpStatus.OK),

  // Card - info messages
  CARD_CREATE_SUCCESS("I-CARD-001", HttpStatus.OK),
  CARD_ARCHIVE_SUCCESS("I-CARD-002", HttpStatus.OK),
  CARD_UPDATE_SUCCESS("I-CARD-003", HttpStatus.OK),
  CARD_MOVE_SUCCESS("I-CARD-004", HttpStatus.OK),
  CARD_ASSIGN_MEMBER_SUCCESS("I-CARD-005", HttpStatus.OK),
  CARD_UNASSIGN_MEMBER_SUCCESS("I-CARD-006", HttpStatus.OK),

  // Label - info message
  LABEL_CREATE_SUCCESS("I-LABEL-001", HttpStatus.OK),
  LABEL_SELECTED_SUCCESS("I-LABEL-002", HttpStatus.OK),
  LABEL_UNSELECTED_SUCCESS("I-LABEL-003", HttpStatus.OK),

  // Checklist - info message
  CHECKLIST_CREATE_SUCCESS("I-CHECKLIST-001", HttpStatus.OK),
  CHECKLIST_ITEM_CREATE_SUCCESS("I-CHECKLIST-002", HttpStatus.OK),
  CHECKLIST_ITEM_TOGGLE_SUCCESS("I-CHECKLIST-003", HttpStatus.OK),
  CHECKLIST_ITEM_DELETE_SUCCESS("I-CHECKLIST-004", HttpStatus.OK),
  CHECKLIST_DELETE_SUCCESS("I-CHECKLIST-005", HttpStatus.OK),

  // Attachment - info message
  ATTACHMENT_UPLOAD_SUCCESS("I-ATTACHMENT-001", HttpStatus.OK),
  ATTACHMENT_DELETE_SUCCESS("I-ATTACHMENT-002", HttpStatus.OK),

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
