package org.example.nextstepbackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.nextstepbackend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

  @Query(
      """
               SELECT t FROM PasswordResetToken t
               WHERE t.expiresAt > :now
                 AND t.used = false
            """)
  List<PasswordResetToken> findAllValidTokens(@Param("now") LocalDateTime now);
}
