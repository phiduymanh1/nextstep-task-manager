package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Integer> {

  List<Label> findByBoardId(Integer boardId);
}
