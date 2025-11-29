package org.example.nextstepbackend.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.nextstepbackend.Enum.BoardRole;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_members",
        uniqueConstraints = {@UniqueConstraint(name = "unique_board_member", columnNames = {"board_id", "user_id"})},
        indexes = {@Index(name = "idx_board_id", columnList = "board_id"), @Index(name = "idx_user_id", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BoardRole role = BoardRole.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;


}
