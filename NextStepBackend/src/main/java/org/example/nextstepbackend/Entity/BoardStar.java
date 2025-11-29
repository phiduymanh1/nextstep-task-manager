package org.example.nextstepbackend.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_stars",
        uniqueConstraints = {@UniqueConstraint(name = "unique_board_star", columnNames = {"board_id", "user_id"})},
        indexes = {@Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_position", columnList = "user_id, position")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardStar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal position;

    @CreationTimestamp
    @Column(name = "starred_at", updatable = false)
    private LocalDateTime starredAt;
}
