package org.example.nextstepbackend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.ForgotPasswordRequest;
import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.dto.request.RegisterRequest;
import org.example.nextstepbackend.dto.request.ResetPasswordRequest;
import org.example.nextstepbackend.dto.response.AuthResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.Auth.AuthService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.example.nextstepbackend.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  public AuthController(ApiResponseUtil apiResponseUtil, AuthService authService, JwtUtil jwtUtil) {
    super(apiResponseUtil);
    this.authService = authService;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
    try {
      Map<String, String> tokens = authService.login(req.email(), req.password());

      // Set refresh token cookie
      Cookie cookie = new Cookie("refreshToken", tokens.get("refreshToken"));
      cookie.setHttpOnly(true);
      cookie.setSecure(false); // deploy HTTPS -> true
      cookie.setPath("/");
      cookie.setMaxAge(7 * 24 * 60 * 60);
      cookie.setAttribute("SameSite", "Strict");
      response.addCookie(cookie);

      AuthResponse authResponse = new AuthResponse(tokens.get("accessToken"));

      return ResponseEntity.ok(success(MessageConst.REFRESH_SUCCESS, authResponse));
    } catch (AuthenticationException ex) {
      return ResponseEntity.status(401).body(error(MessageConst.INVALID_CREDENTIALS));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
      @CookieValue(value = "refreshToken", required = false) String refreshToken) {
    if (refreshToken == null || !jwtUtil.isRefreshTokenValid(refreshToken)) {
      return ResponseEntity.status(401).body(error(MessageConst.REFRESH_TOKEN_INVALID));
    }

    String newAccessToken = authService.refreshToken(refreshToken);

    AuthResponse authResponse = new AuthResponse(newAccessToken);

    return ResponseEntity.ok(success(MessageConst.REFRESH_SUCCESS, authResponse));
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
    authService.register(req);

    return ResponseEntity.ok(success(MessageConst.REGISTER_SUCCESS, null));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request.email());

    return ResponseEntity.ok(success(MessageConst.SEND_LINK_FORGOT_PASSWORD, null));
  }

  @PostMapping("/reset-password")
  public void resetPassword(@RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.token(), request.newPassword());
  }
}
