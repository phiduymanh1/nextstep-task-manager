package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Activity;
import org.example.nextstepbackend.enums.ActionType;
import org.example.nextstepbackend.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {

  Page<Activity> findByCardId(Integer cardId, Pageable pageable);

  void deleteByEntityIdAndActionType(Integer entityId, ActionType actionType);

  @Modifying
  @Transactional
  void deleteByCard_IdAndEntityTypeAndEntityIdAndActionType(
      Integer cardId, EntityType entityType, Integer entityId, ActionType actionType);
}
