package org.example.nextstepbackend.controller;

import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.services.user.UserService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public ResponseEntity<ApiResponse<UserResponse>> getUserMe(
      @AuthenticationPrincipal UserDetails userDetails) {
    if (!StringUtils.hasText(userDetails.getUsername())) {
      throw new InvalidInputException("Email parameter is required");
    }
    UserResponse userResponse = userService.getUserMe(userDetails.getUsername());
    if (userResponse == null) {
      throw new ResourceNotFoundException("User not found");
    }
    return ResponseEntity.ok(success(null, userResponse));
  }
}
