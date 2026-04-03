package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {

  List<Checklist> findByCardIdOrderByPosition(Integer cardId);
}
