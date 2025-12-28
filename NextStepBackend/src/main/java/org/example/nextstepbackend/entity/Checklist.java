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
import org.example.nextstepbackend.entity.embedded.CreateAudit;

@Entity
@Table(
    name = "checklists",
    indexes = {
      @Index(name = "idx_card_id", columnList = "card_id"),
      @Index(name = "idx_position", columnList = "card_id, position")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Checklist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", nullable = false)
  private Card card;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal position;

  @Embedded private CreateAudit audit;

  @OneToMany(
      mappedBy = "checklist",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<ChecklistItem> items = new HashSet<>();

  public void addItem(ChecklistItem item) {
    items.add(item);
    item.setChecklist(this);
  }
}
