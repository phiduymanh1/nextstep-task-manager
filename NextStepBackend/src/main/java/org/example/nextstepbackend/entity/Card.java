package org.example.nextstepbackend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "cards",
    indexes = {
      @Index(name = "idx_list_id", columnList = "list_id"),
      @Index(name = "idx_position", columnList = "list_id, position"),
      @Index(name = "idx_due_date", columnList = "due_date"),
      @Index(name = "idx_archived", columnList = "is_archived"),
      @Index(name = "idx_created_by", columnList = "created_by")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "list_id", nullable = false)
  private ListEntity list;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal position;

  @Column(name = "due_date")
  private LocalDateTime dueDate;

  @Column(name = "due_reminder")
  @Builder.Default
  private Boolean dueReminder = false;

  @Column(name = "is_completed")
  @Builder.Default
  private Boolean isCompleted = false;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "cover_color", length = 20)
  private String coverColor;

  @Column(name = "cover_image_url", length = 500)
  private String coverImageUrl;

  @Column(name = "is_archived")
  @Builder.Default
  private Boolean isArchived = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // relations
  @OneToMany(
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<CardMember> members = new HashSet<>();

  @OneToMany(
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Checklist> checklists = new HashSet<>();

  @OneToMany(
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Attachment> attachments = new HashSet<>();

  @OneToMany(
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Comment> comments = new HashSet<>();

  @OneToMany(
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<CardLabel> cardLabels = new HashSet<>();

  @OneToMany(
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<CardCustomFieldValue> customFieldValues = new HashSet<>();

  public void addMember(CardMember m) {
    members.add(m);
    m.setCard(this);
  }
}
