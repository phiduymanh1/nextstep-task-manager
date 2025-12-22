package org.example.nextstepbackend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.enums.Visibility;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "boards",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "unique_board_slug",
          columnNames = {"workspace_id", "slug"})
    },
    indexes = {
      @Index(name = "idx_workspace_id", columnList = "workspace_id"),
      @Index(name = "idx_is_closed", columnList = "is_closed")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 100)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "background_color", length = 20)
  @Builder.Default
  private String backgroundColor = "#0079BF";

  @Column(name = "background_image_url", length = 500)
  private String backgroundImageUrl;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Visibility visibility = Visibility.WORKSPACE;

  @Column(name = "is_closed")
  @Builder.Default
  private Boolean isClosed = false;

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
      mappedBy = "board",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<BoardMember> members = new HashSet<>();

  @OneToMany(
      mappedBy = "board",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<ListEntity> lists = new HashSet<>();

  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<BoardStar> stars = new HashSet<>();

  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Label> labels = new HashSet<>();
}
