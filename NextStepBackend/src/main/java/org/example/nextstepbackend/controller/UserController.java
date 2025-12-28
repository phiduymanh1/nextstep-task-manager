package org.example.nextstepbackend.controller;

import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.services.user.UserService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

  private final UserService userService;

  protected UserController(ApiResponseUtil responseUtil, UserService userService) {
    super(responseUtil);
    this.userService = userService;
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getUserMe(@RequestParam String email) {
    UserResponse userResponse = userService.getUserMe(email);
    return ResponseEntity.ok(success(null, userResponse));
  }
}
