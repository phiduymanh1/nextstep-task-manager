package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.UserUpdateRequest;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.services.user.UserService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

  private final UserService userService;

  protected UserController(ApiResponseUtil responseUtil, UserService userService) {
    super(responseUtil);
    this.userService = userService;
  }

  /** Get current user info */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getUserMe(
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      throw new InvalidInputException("Unauthenticated");
    }
    UserResponse userResponse = userService.getUserMe(userDetails.getUsername());
    return ResponseEntity.ok(success(null, userResponse));
  }

  /** Get user info by id - Admin only */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") Integer id) {
    UserResponse userResponse = userService.getUserById(id);
    return ResponseEntity.ok(success(null, userResponse));
  }

  /** Update current user info */
  @PatchMapping("/me")
  public ResponseEntity<ApiResponse<Void>> patchUserMe(
      @Valid @RequestBody UserUpdateRequest request) {
    userService.updateUserMe(request);
    return ResponseEntity.ok(success(MessageConst.USER_UPDATE_SUCCESS, null));
  }

  /** Update current user avatar */
  @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<String>> updateAvatar(@RequestPart("file") MultipartFile file) {

    userService.updateAvatar(file);
    return ResponseEntity.ok(success(MessageConst.USER_AVATAR_UPDATE_SUCCESS, null));
  }
}
