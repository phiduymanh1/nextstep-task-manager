package org.example.nextstepbackend.controller.base;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.utils.ApiResponseUtil;

@RequiredArgsConstructor
public class BaseController {
  protected final ApiResponseUtil responseUtil;

  // success
  protected <T> ApiResponse<T> success(MessageConst msg, T data) {
    return responseUtil.success(msg, data);
  }

  // error 1 message
  protected ApiResponse<Void> error(MessageConst msg) {
    return responseUtil.error(msg);
  }

  // error multi message
  protected ApiResponse<Void> error(String code, String message, List<String> errors) {
    return responseUtil.error(code, message, errors);
  }

  // validation error
  protected ApiResponse<Void> validationError(List<String> errors) {
    return responseUtil.validationError(errors);
  }
}
