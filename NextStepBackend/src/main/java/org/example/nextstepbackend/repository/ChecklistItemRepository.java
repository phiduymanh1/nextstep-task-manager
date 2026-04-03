package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Integer> {

  List<ChecklistItem> findByChecklist_Card_Id(Integer cardId);
}
