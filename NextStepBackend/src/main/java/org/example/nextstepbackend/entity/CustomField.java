package org.example.nextstepbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.nextstepbackend.enums.CustomFieldType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "custom_fields", indexes = {
        @Index(name = "idx_board_id", columnList = "board_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomFieldType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> options; // For dropdown type

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal position;

    // Relationships
    @OneToMany(mappedBy = "customField", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CardCustomFieldValue> cardValues = new HashSet<>();
}
