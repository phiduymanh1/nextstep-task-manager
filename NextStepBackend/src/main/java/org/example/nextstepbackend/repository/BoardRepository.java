package org.example.nextstepbackend.repository;

import java.util.Optional;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardRepository extends JpaRepository<Board, Integer> {

  Page<Board> findByWorkspaceSlug(String slug, Pageable pageable);

  boolean existsByWorkspaceAndSlug(Workspace workspace, String slug);

  Optional<Board> findByWorkspace_SlugAndSlug(String workspaceSlug, String boardSlug);

  Optional<Board> findBySlug(String slug);

  @Query(
      """
    SELECT b FROM Board b
    LEFT JOIN FETCH b.members m
    LEFT JOIN FETCH m.user u
    WHERE b.slug = :slug
      AND (
        (b.visibility = 'PUBLIC')
        OR (b.visibility = 'WORKSPACE' AND u.email = :email)
        OR (b.visibility = 'PRIVATE' AND b.createdBy.email = :email)
      )
""")
  Optional<Board> findBoardBySlugAndMember(String slug, String email);
}
