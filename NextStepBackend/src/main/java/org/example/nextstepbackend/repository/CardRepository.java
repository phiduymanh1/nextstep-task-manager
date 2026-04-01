package org.example.nextstepbackend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.example.nextstepbackend.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<Card, Integer> {

  List<Card> findByListIdAndIsArchivedFalseOrderByPositionAsc(Integer listId);

  @Query("SELECT MAX(c.position) FROM Card c WHERE c.list.id = :listId")
  Optional<BigDecimal> findMaxPositionByListId(Integer listId);

  List<Card> findByIdIn(Set<Integer> ids);
}
