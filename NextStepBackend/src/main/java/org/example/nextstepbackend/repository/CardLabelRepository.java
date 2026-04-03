package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardLabelRepository extends JpaRepository<CardLabel, Integer> {

  List<CardLabel> findByCardId(Integer cardId);
}
