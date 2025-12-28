package org.example.nextstepbackend.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.FullAudit;
import org.example.nextstepbackend.enums.Visibility;

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

  @Embedded private FullAudit audit;

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
