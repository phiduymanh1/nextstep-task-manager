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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.enums.UserRole;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_email", columnList = "email"),
      @Index(name = "idx_username", columnList = "username")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name", length = 100)
  private String fullName;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(unique = true, length = 15)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserRole role = UserRole.USER;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Relationships
  @OneToOne(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private UserProfile profile;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<WorkspaceMember> workspaceMemberships = new HashSet<>();

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<BoardMember> boardMemberships = new HashSet<>();

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<CardMember> cardAssignments = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<BoardStar> starredBoards = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<AuthToken> authTokens = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Notification> notifications = new HashSet<>();

  // Helpers
  public void addWorkspaceMembership(WorkspaceMember membership) {
    workspaceMemberships.add(membership);
    membership.setUser(this);
  }

  public void addBoardMembership(BoardMember membership) {
    boardMemberships.add(membership);
    membership.setUser(this);
  }

  public void addCardAssignment(CardMember cm) {
    cardAssignments.add(cm);
    cm.setUser(this);
  }

  public void addAuthToken(AuthToken token) {
    authTokens.add(token);
    token.setUser(this);
  }

  public void addNotification(Notification n) {
    notifications.add(n);
    n.setUser(this);
  }
}
