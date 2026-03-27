package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Integer> {

  Page<Board> findByWorkspaceSlug(String slug, Pageable pageable);
}
