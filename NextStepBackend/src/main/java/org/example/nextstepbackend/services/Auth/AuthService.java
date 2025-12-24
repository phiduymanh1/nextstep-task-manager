package org.example.nextstepbackend.services.Auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.RegisterRequest;
import org.example.nextstepbackend.entity.PasswordResetToken;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.AuthException;
import org.example.nextstepbackend.exceptions.InvalidTokenException;
import org.example.nextstepbackend.mappers.UserMapper;
import org.example.nextstepbackend.repository.PasswordResetTokenRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.Mail.MailService;
import org.example.nextstepbackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Value("${app.frontend.base-url:http://localhost:8080}")
  private String frontendBaseUrl;

  @Value("${app.password-reset.token-ttl-minutes:15}")
  private long tokenTtlMinutes;

  public Map<String, String> login(String userEmail, String password) {
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userEmail, password));
    // If authenticate success, create JWT
    String accessToken = jwtUtil.generateAccessToken(auth.getName());
    String refreshToken = jwtUtil.generateRefreshToken(auth.getName());

    // Return both tokens, controller will set cookies
    return Map.of(
        "accessToken", accessToken,
        "refreshToken", refreshToken);
  }

  // refresh token
  public String refreshToken(String refreshToken) {
    if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
      throw new RuntimeException("Invalid or expired refresh token");
    }
    String username = jwtUtil.extractUserName(refreshToken);

    if (!userRepository.existsByEmail(username)) {
      throw new AuthException("User not found");
    }

    return jwtUtil.generateAccessToken(username);
  }

  // register
  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already in use");
    }
    User user = userMapper.toUser(request);

    String passwordHash = passwordEncoder.encode(request.password());
    user.setPasswordHash(passwordHash);

    userRepository.save(user);
  }

  // forgot password
  public void forgotPassword(String email) {
    userRepository
        .findByEmail(email)
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

  // reset password
  @Transactional
  public void resetPassword(String rawToken, String newPassword) {

    List<PasswordResetToken> tokens =
        passwordResetTokenRepository.findAllValidTokens(LocalDateTime.now());

    PasswordResetToken resetToken =
        tokens.stream()
            .filter(t -> passwordEncoder.matches(rawToken, t.getToken()))
            .findFirst()
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

    if (resetToken.getUsed()) {
      throw new IllegalStateException("Token already used");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    resetToken.setUsed(true);
    passwordResetTokenRepository.save(resetToken);
  }
}
