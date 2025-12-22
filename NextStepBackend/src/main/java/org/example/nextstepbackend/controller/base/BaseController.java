package org.example.nextstepbackend.controller.base;

import java.util.List;
import org.example.nextstepbackend.dto.response.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.utils.ApiResponseUtil;

public class BaseController {
  protected final ApiResponseUtil responseUtil;

  protected BaseController(ApiResponseUtil responseUtil) {
    this.responseUtil = responseUtil;
  }

  // success
  protected <T> ApiResponse<T> success(MessageConst msg, T data) {
    return responseUtil.success(msg, data);
  }

  // error 1 message
  protected ApiResponse<?> error(MessageConst msg) {
    return responseUtil.error(msg);
  }

  // error multi message
  protected ApiResponse<?> error(String code, String message, List<String> errors) {
    return responseUtil.error(code, message, errors);
  }

  // validation error
  protected ApiResponse<?> validationError(List<String> errors) {
    return responseUtil.validationError(errors);
  }
}
