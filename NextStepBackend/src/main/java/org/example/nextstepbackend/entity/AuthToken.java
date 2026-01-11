package org.example.nextstepbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.CreateAudit;

@Entity
@Table(name = "auth_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, unique = true, length = 255)
  private String token;

  @Column(name = "refresh_token", unique = true, length = 500)
  private String refreshToken;

  @Column(name = "device_info", columnDefinition = "TEXT")
  private String deviceInfo;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Embedded private CreateAudit createAudit;

  // Business methods
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !isExpired() && user != null && user.getIsActive();
  }

  // Factory method
  public static AuthToken create(
      User user,
      String token,
      String refreshToken,
      String deviceInfo,
      String ipAddress,
      int expiresInHours) {
    return AuthToken.builder()
        .user(user)
        .token(token)
        .refreshToken(refreshToken)
        .deviceInfo(deviceInfo)
        .ipAddress(ipAddress)
        .expiresAt(LocalDateTime.now().plusHours(expiresInHours))
        .build();
  }
}
