package org.example.nextstepbackend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.FullAudit;
import org.example.nextstepbackend.enums.Visibility;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "deleted")
public class Workspace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 100)
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
    if (m == null) return;

    this.members.add(m);

    if (m.getWorkspace() != this) {
      m.setWorkspace(this);
    }
  }

  public void addBoard(Board b) {
    if (b == null) return;

    this.boards.add(b);
    if (b.getWorkspace() != this) {
      b.setWorkspace(this);
    }
  }
}
