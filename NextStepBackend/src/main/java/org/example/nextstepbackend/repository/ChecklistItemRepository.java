package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Integer> {

  List<ChecklistItem> findByChecklist_Card_Id(Integer cardId);
}
