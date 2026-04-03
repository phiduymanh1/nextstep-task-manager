package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {

  List<Checklist> findByCardIdOrderByPosition(Integer cardId);
}
