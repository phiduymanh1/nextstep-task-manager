package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Activity;
import org.example.nextstepbackend.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {

  Page<Activity> findByCardId(Integer cardId, Pageable pageable);

  void deleteByEntityIdAndActionType(Integer entityId, ActionType actionType);
}
