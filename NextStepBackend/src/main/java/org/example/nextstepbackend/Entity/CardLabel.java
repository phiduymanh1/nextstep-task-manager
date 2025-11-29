package org.example.nextstepbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_labels",
        uniqueConstraints = {@UniqueConstraint(name = "unique_card_label", columnNames = {"card_id", "label_id"})},
        indexes = {@Index(name = "idx_card_id", columnList = "card_id"), @Index(name = "idx_label_id", columnList = "label_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;
}
