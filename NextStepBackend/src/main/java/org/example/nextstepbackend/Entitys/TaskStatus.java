package org.example.nextstepbackend.Entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 20)
    private String color;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Relationships
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();
}
