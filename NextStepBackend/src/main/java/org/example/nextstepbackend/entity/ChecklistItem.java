package org.example.nextstepbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "checklist_items", indexes = {
        @Index(name = "idx_checklist_id", columnList = "checklist_id"),
        @Index(name = "idx_position", columnList = "checklist_id, position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal position;

    @Column(name = "due_date")
    private LocalDateTime dueDate;
}
