package org.example.nextstepbackend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nextstepbackend.entity.embedded.FullAudit;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@SuppressWarnings("deprecation")
@Entity
@Table(name = "lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE lists SET is_archived = true WHERE id = ?")
@Where(clause = "is_archived = false")
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
