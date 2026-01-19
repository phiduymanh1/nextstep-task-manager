package org.example.nextstepbackend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.ForgotPasswordRequest;
import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.dto.request.RegisterRequest;
import org.example.nextstepbackend.dto.request.ResetPasswordRequest;
import org.example.nextstepbackend.dto.response.auth.AuthResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

  private final AuthService authService;

  public AuthController(ApiResponseUtil apiResponseUtil, AuthService authService) {
    super(apiResponseUtil);
    this.authService = authService;
  }

  /** Login api */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest req, HttpServletResponse response) {
    Map<String, String> tokens = authService.login(req.email(), req.password());

    // Set refresh token cookie
    Cookie cookie = new Cookie(Const.TEXT_REFRESH_TOKEN, tokens.get(Const.TEXT_REFRESH_TOKEN));
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // deploy HTTPS -> true
    cookie.setPath("/");
    cookie.setMaxAge(7 * 24 * 60 * 60);
    cookie.setAttribute("SameSite", "Strict");
    response.addCookie(cookie);

    AuthResponse authResponse = new AuthResponse(tokens.get(Const.TEXT_ACCESS_TOKEN));

    return ResponseEntity.ok(success(MessageConst.AUTH_LOGIN_SUCCESS, authResponse));
  }

  /** Refresh token api */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthResponse>> refresh(
      @CookieValue(value = Const.TEXT_REFRESH_TOKEN, required = false) String refreshToken) {
    String newAccessToken = authService.refreshToken(refreshToken);

    AuthResponse authResponse = new AuthResponse(newAccessToken);

    return ResponseEntity.ok(success(MessageConst.AUTH_REFRESH_SUCCESS, authResponse));
  }

  /** Register api */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest req) {
    authService.register(req);

    return ResponseEntity.ok(success(MessageConst.AUTH_REGISTER_SUCCESS, null));
  }

  /** Forgot password api */
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request.email());

    return ResponseEntity.ok(success(MessageConst.AUTH_FORGOT_PASSWORD_SENT, null));
  }

  /** Reset password api */
  @PostMapping("/reset-password")
  public void resetPassword(@RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.token(), request.newPassword());
  }
}
