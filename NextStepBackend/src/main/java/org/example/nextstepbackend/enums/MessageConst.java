package org.example.nextstepbackend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MessageConst {

    // Info message
    REFRESH_SUCCESS("I0001", HttpStatus.OK),
    REGISTER_SUCCESS("I0002", HttpStatus.OK),
    SEND_LINK_FORGOT_PASSWORD("I0003", HttpStatus.OK),

    // Error messages
    INVALID_CREDENTIALS("E0001", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("E0002", HttpStatus.UNAUTHORIZED),
    AUTH_FAILED("E0003", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("E0004", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("E0005", HttpStatus.FORBIDDEN),
    INTERNAL_ERROR("E0006", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final String code;
    private final HttpStatus httpStatus;

    MessageConst(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

}
