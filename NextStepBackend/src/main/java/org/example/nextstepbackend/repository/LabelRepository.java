package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Integer> {

  List<Label> findByBoardId(Integer boardId);
}
