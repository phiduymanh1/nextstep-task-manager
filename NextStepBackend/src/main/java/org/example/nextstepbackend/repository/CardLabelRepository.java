package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.CardLabel;
import org.example.nextstepbackend.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardLabelRepository extends JpaRepository<CardLabel, Integer> {

  List<CardLabel> findByCardId(Integer cardId);

  boolean existsByCardAndLabel(Card card, Label label);

  void deleteByCardAndLabel(Card card, Label label);
}
