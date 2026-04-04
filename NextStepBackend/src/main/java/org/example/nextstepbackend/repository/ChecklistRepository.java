package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {

  List<Checklist> findByCardIdOrderByPosition(Integer cardId);

  @Query("SELECT MAX(c.position) FROM Checklist c WHERE c.card.id = :cardId")
  Optional<BigDecimal> findMaxPositionByCardId(@Param("cardId") Integer cardId);

  List<Checklist> findByIdIn(Set<Integer> checklistId);

  List<Checklist> findByCardIdOrderByPositionAsc(Integer cardId);
}
