package org.example.nextstepbackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtUtil {

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.access-token-expiration}")
  private Long expirationAccessToken;

  @Value("${app.jwt.refresh-token-expiration}")
  private Long expirationRefreshToken;

  /** Get signing key from secret */
  private Key getKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  /** Generate access token */
  public String generateAccessToken(String username) {
    return buildToken(username, expirationAccessToken);
  }

  /** Generate refresh token */
  public String generateRefreshToken(String username) {
    return buildToken(username, expirationRefreshToken);
  }

  /** Build JWT token */
  public String buildToken(String username, Long exp) {
    return Jwts.builder()
        .setSubject(username)
        .setId(UUID.randomUUID().toString()) // TODO: add blacklist token
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + exp))
        .signWith(getKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /** Extract username from token */
  public String extractUserName(String token) {
    return parseClaims(token).map(Claims::getSubject).orElse(null);
  }

  /** Validate access token */
  public boolean isAccessTokenValid(String token, UserDetails userDetails) {
    return parseClaims(token)
        .map(
            c ->
                c.getSubject().equals(userDetails.getUsername())
                    && c.getExpiration().after(new Date()))
        .orElse(false);
  }

  /** Validate refresh token */
  public boolean isRefreshTokenValid(String token) {
    return parseClaims(token).map(c -> c.getExpiration().after(new Date())).orElse(false);
  }

  /** Parse claims from token */
  private Optional<Claims> parseClaims(String token) {
    try {
      return Optional.of(
          Jwts.parserBuilder() // bat dau qua trinh giai ma jwt
              .setSigningKey(getKey()) // xac thuc chu ky
              .build() // xay dung doi tuong JwtParser
              .parseClaimsJws(token) // giai ma token
              .getBody()); // trich xuat doi tuong claims
    } catch (JwtException e) {
      return Optional.empty();
    }
  }

  /** Get JTI from token */
  public String getJti(String token) {
    return parseClaims(token).map(Claims::getId).orElse(null);
  }

  /** Get remaining time from token */
  public long getRemainingTime(String token) {
    return parseClaims(token)
        .map(Claims::getExpiration)
        .map(exp -> exp.getTime() - System.currentTimeMillis())
        .orElse(0L);
  }
}
