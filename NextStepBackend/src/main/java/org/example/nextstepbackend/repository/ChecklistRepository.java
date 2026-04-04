package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {

  List<Checklist> findByCardIdOrderByPosition(Integer cardId);

  Optional<BigDecimal> findMaxPositionByCardId(Integer cardId);

  List<Checklist> findByIdIn(Set<Integer> checklistId);

  List<Checklist> findByCardIdOrderByPositionAsc(Integer cardId);
}
