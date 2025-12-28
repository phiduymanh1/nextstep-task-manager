package org.example.nextstepbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "card_custom_field_values",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_card_field",
            columnNames = {"card_id", "custom_field_id"}),
    indexes = {
      @Index(name = "idx_card_id", columnList = "card_id"),
      @Index(name = "idx_custom_field_id", columnList = "custom_field_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCustomFieldValue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", nullable = false)
  private Card card;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "custom_field_id", nullable = false)
  private CustomField customField;

  @Column(columnDefinition = "TEXT")
  private String value;

  // Helper methods to parse value based on field type
  public String getTextValue() {
    return value;
  }

  public Integer getNumberValue() {
    return value != null ? Integer.parseInt(value) : null;
  }

  public LocalDate getDateValue() {
    return value != null ? LocalDate.parse(value) : null;
  }

  public Boolean getCheckboxValue() {
    return value != null ? Boolean.parseBoolean(value) : null;
  }
}
