package org.example.nextstepbackend.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lists", indexes = {
        @Index(name = "idx_board_id", columnList = "board_id"),
        @Index(name = "idx_position", columnList = "board_id, position"),
        @Index(name = "idx_archived", columnList = "is_archived")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal position;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Card> cards = new HashSet<>();

    public void addCard(Card c) {
        cards.add(c);
        c.setList(this);
    }
}
