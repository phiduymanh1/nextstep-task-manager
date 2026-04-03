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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.CreateAudit;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // Many notifications belong to 1 user
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 50)
  private String type; // card_assigned, card_due, comment_mention, ...

  @Column(nullable = false, length = 255)
  private String title;

  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  @Column(name = "entity_type", length = 50)
  private String entityType; // card, board, comment

  @Column(name = "entity_id")
  private Integer entityId;

  @Column(name = "link_url", length = 500)
  private String linkUrl; // URL click

  @Column(name = "is_read")
  @Builder.Default
  private Boolean isRead = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Embedded private CreateAudit audit;
}
