package org.example.nextstepbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "activities",
    indexes = {
      @Index(name = "idx_card_id", columnList = "card_id"),
      @Index(name = "idx_board_id", columnList = "board_id"),
      @Index(name = "idx_user_id", columnList = "user_id"),
      @Index(name = "idx_created_at", columnList = "created_at"),
      @Index(name = "idx_action_type", columnList = "action_type")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id")
  private Card card;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  private Board board;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "action_type", length = 50, nullable = false)
  private String actionType;

  @Column(name = "entity_type", length = 50, nullable = false)
  private String entityType;

  @Column(name = "entity_id")
  private Integer entityId;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Column(columnDefinition = "JSON")
  private String metadata;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
