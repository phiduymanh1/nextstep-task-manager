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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.FullAudit;
import org.example.nextstepbackend.enums.UserRole;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
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

  @Column(name = "avatar_public_id", length = 255)
  private String avatarPublicId;

  @Column(unique = true, length = 15)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserRole role = UserRole.USER;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Embedded private FullAudit audit;

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
    if (membership == null) return;

    this.workspaceMemberships.add(membership);
    // Important: Only reverse the setting if it hasn't already been set to avoid an infinite loop.
    if (membership.getUser() != this) {
      membership.setUser(this);
    }
  }

  public void addBoardMembership(BoardMember membership) {
    if (membership == null) return;
    this.boardMemberships.add(membership);
    if (membership.getUser() != this) {
      membership.setUser(this);
    }
  }

  public void addCardAssignment(CardMember cm) {
    if (cm == null) return;
    this.cardAssignments.add(cm);
    if (cm.getUser() != this) {
      cm.setUser(this);
    }
  }

  public void addAuthToken(AuthToken token) {
    if (token == null) return;
    this.authTokens.add(token);
    if (token.getUser() != this) {
      token.setUser(this);
    }
  }

  public void addNotification(Notification n) {
    if (n == null) return;
    this.notifications.add(n);
    if (n.getUser() != this) {
      n.setUser(this);
    }
  }
}
