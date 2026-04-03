package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

  @Query(
      """
               SELECT t FROM PasswordResetToken t
               WHERE t.expiresAt > :now
                 AND t.used = false
            """)
  List<PasswordResetToken> findAllValidTokens(@Param("now") LocalDateTime now);
}
