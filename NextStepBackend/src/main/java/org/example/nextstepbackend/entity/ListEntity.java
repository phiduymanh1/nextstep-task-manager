package org.example.nextstepbackend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.FullAudit;

@Entity
@Table(
    name = "lists",
    indexes = {
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

  @Embedded private FullAudit audit;

  @OneToMany(
      mappedBy = "list",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Card> cards = new HashSet<>();

  public void addCard(Card c) {
    cards.add(c);
    c.setList(this);
  }
}
