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
    name = "workspaces",
    indexes = {
      @Index(name = "idx_slug", columnList = "slug"),
      @Index(name = "idx_created_by", columnList = "created_by")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Visibility visibility = Visibility.PRIVATE;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @Embedded private FullAudit audit;

  // Relations
  @OneToMany(
      mappedBy = "workspace",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<WorkspaceMember> members = new HashSet<>();

  @OneToMany(
      mappedBy = "workspace",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Board> boards = new HashSet<>();

  // helpers
  public void addMember(WorkspaceMember m) {
    members.add(m);
    m.setWorkspace(this);
  }

  public void addBoard(Board b) {
    boards.add(b);
    b.setWorkspace(this);
  }
}
