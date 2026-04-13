package org.example.nextstepbackend.repository;

import java.util.Optional;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardRepository extends JpaRepository<Board, Integer> {

  @Query(
      """
  SELECT DISTINCT b FROM Board b
  LEFT JOIN b.members bm
  LEFT JOIN bm.user u
  WHERE b.workspace.slug = :slug
  AND (
      b.visibility = org.example.nextstepbackend.enums.BoardVisibility.PUBLIC
      OR (
          b.visibility = org.example.nextstepbackend.enums.BoardVisibility.WORKSPACE
          AND (
              b.createdBy.email = :email
              OR (
                  u.email = :email
                  AND bm.role IN (
                      org.example.nextstepbackend.enums.BoardRole.OBSERVER,
                      org.example.nextstepbackend.enums.BoardRole.ADMIN,
                      org.example.nextstepbackend.enums.BoardRole.MEMBER
                  )
              )
          )
      )
      OR (
          b.visibility = org.example.nextstepbackend.enums.BoardVisibility.PRIVATE
          AND (
              b.createdBy.email = :email
              OR EXISTS (
                  SELECT 1 FROM BoardMember bm2
                  JOIN bm2.user u2
                  WHERE bm2.board.id = b.id
                  AND u2.email = :email
                  AND bm2.role IN (
                      org.example.nextstepbackend.enums.BoardRole.OBSERVER,
                      org.example.nextstepbackend.enums.BoardRole.ADMIN,
                      org.example.nextstepbackend.enums.BoardRole.MEMBER
                  )
              )
          )
      )
  )
  """)
  Page<Board> findByWorkspaceSlug(String slug, String email, Pageable pageable);

  boolean existsByWorkspaceAndSlug(Workspace workspace, String slug);

  Optional<Board> findByWorkspace_SlugAndSlug(String workspaceSlug, String boardSlug);

  Optional<Board> findBySlug(String slug);

  @Query(
      """
    SELECT DISTINCT b FROM Board b
    LEFT JOIN FETCH b.members m
    LEFT JOIN FETCH m.user u
    WHERE b.slug = :slug
    AND (
        b.visibility = 'PUBLIC'

        OR (
            b.visibility = 'WORKSPACE'
            AND (
                b.createdBy.email = :email
                OR u.email = :email
            )
        )

        OR (
            b.visibility = 'PRIVATE'
            AND (
                b.createdBy.email = :email
                OR u.email = :email
            )
        )
    )
    """)
  Optional<Board> findBoardAccessible(String slug, String email);
}
