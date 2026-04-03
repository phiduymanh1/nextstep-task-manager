package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.ListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ListsRepository extends JpaRepository<ListEntity, Integer> {

  List<ListEntity> findByBoardIdOrderByPositionAsc(Integer boardId);

  @Query("SELECT MAX(l.position) FROM ListEntity l WHERE l.board.id = :boardId")
  BigDecimal findMaxPositionByBoardId(@Param("boardId") Integer boardId);

  Page<ListEntity> findByBoardSlug(String slug, Pageable pageable);

  Optional<ListEntity> findByBoard_SlugAndId(String boardSlug, Integer id);

  @Query(
      """
  SELECT l FROM ListEntity l
  JOIN l.board b
  JOIN b.members m
  WHERE l.id = :listId AND m.user.email = :email
""")
  Optional<ListEntity> findByIdAndMember(Integer listId, String email);
}
