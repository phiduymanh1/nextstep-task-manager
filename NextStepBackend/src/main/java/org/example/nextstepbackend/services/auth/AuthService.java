package org.example.nextstepbackend.services.auth;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.RegisterRequest;
import org.example.nextstepbackend.entity.PasswordResetToken;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.AppException;
import org.example.nextstepbackend.exceptions.AuthException;
import org.example.nextstepbackend.exceptions.DuplicateResourceException;
import org.example.nextstepbackend.exceptions.InvalidTokenException;
import org.example.nextstepbackend.mappers.UserMapper;
import org.example.nextstepbackend.repository.PasswordResetTokenRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.mail.MailService;
import org.example.nextstepbackend.services.security.CustomUserDetails;
import org.example.nextstepbackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final MailService mailService;
  private final StringRedisTemplate stringRedisTemplate;

  @Value("${app.frontend.base-url:http://localhost:8080}")
  private String frontendBaseUrl;

  @Value("${app.password-reset.token-ttl-minutes:15}")
  private long tokenTtlMinutes;

  /** login */
  public Map<String, String> login(String userEmail, String password) {
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userEmail, password));
    // If authenticate success, create JWT
    String accessToken = jwtUtil.generateAccessToken(auth.getName());
    String refreshToken = jwtUtil.generateRefreshToken(auth.getName());

    // Return both tokens, controller will set cookies
    return Map.of(Const.TEXT_ACCESS_TOKEN, accessToken, Const.TEXT_REFRESH_TOKEN, refreshToken);
  }

  /** refresh token */
  public String refreshToken(String refreshToken) {
    if (refreshToken == null || !jwtUtil.isRefreshTokenValid(refreshToken)) {
      throw new AuthException("Invalid refresh token");
    }
    String username = jwtUtil.extractUserName(refreshToken);

    if (!userRepository.existsByEmail(username)) {
      throw new AuthException("User not found");
    }

    return jwtUtil.generateAccessToken(username);
  }

  /** register */
  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new DuplicateResourceException("Email already in use");
    }
    User user = userMapper.toUser(request);

    String passwordHash = passwordEncoder.encode(request.password());
    user.setPasswordHash(passwordHash);

    userRepository.save(user);
  }

  /** forgot password */
  public void forgotPassword(String email) {
    userRepository
        .findByEmail(email)
        .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
        .ifPresent(
            user -> {
              String rawToken = UUID.randomUUID().toString();
              String tokenHash = passwordEncoder.encode(rawToken);

              PasswordResetToken resetToken =
                  PasswordResetToken.builder()
                      .user(user)
                      .token(tokenHash)
                      .expiresAt(LocalDateTime.now().plusMinutes(tokenTtlMinutes))
                      .used(false)
                      .build();

              passwordResetTokenRepository.save(resetToken);

              String resetLink = frontendBaseUrl + "/reset-password?token=" + rawToken;

              mailService.sendMail(
                  user.getEmail(), "Reset your password", "Click link:\n" + resetLink);
            });
  }

  /** reset password */
  @Transactional
  public void resetPassword(String rawToken, String newPassword) {

    List<PasswordResetToken> tokens =
        passwordResetTokenRepository.findAllValidTokens(LocalDateTime.now());

    PasswordResetToken resetToken =
        tokens.stream()
            .filter(t -> passwordEncoder.matches(rawToken, t.getToken()))
            .findFirst()
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

    if (resetToken.isUsed()) {
      throw new IllegalStateException("Token already used");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    resetToken.setUsed(true);
    passwordResetTokenRepository.save(resetToken);
  }

  /** Logout */
  public void logout(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) return;

    String jti = jwtUtil.getJti(refreshToken);
    long ttl = jwtUtil.getRemainingTime(refreshToken);

    /**
     * Store a dummy flag value ("1") to mark the token as revoked only key existence matters, value
     * has no business meaning
     */
    if (ttl > 0) {
      stringRedisTemplate
          .opsForValue()
          .set("blacklist:refresh:" + jti, "1", ttl, TimeUnit.MILLISECONDS);
    }
  }

  /** get current logged-in user */
  public User getCurrentUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (!(principal instanceof CustomUserDetails userDetails)) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Unauthorized", "UNAUTHORIZED-FAILED");
    }

    return userRepository
        .findById(userDetails.getId())
        .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Unauthorized"));
  }

  /** get current logged-in user id */
  public Integer getCurrentUserId() {
    return getCurrentUser().getId();
  }
}
