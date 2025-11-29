package org.example.nextstepbackend.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.nextstepbackend.Enum.Visibility;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "workspaces", indexes = {
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<WorkspaceMember> members = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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
