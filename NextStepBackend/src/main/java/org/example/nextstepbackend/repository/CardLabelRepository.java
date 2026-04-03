package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardLabelRepository extends JpaRepository<CardLabel, Integer> {

  List<CardLabel> findByCardId(Integer cardId);
}
