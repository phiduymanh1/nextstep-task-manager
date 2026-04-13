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
        LEFT JOIN FETCH b.members bm
        LEFT JOIN FETCH bm.user u
        WHERE b.workspace.slug = :slug
          AND b.visibility IN (
              org.example.nextstepbackend.enums.Visibility.PUBLIC,
              org.example.nextstepbackend.enums.Visibility.WORKSPACE
          )
    """)
  Page<Board> findPublicAndWorkspaceBoards(String slug, Pageable pageable);

  @Query(
      """
        SELECT DISTINCT b FROM Board b
        WHERE b.workspace.slug = :slug
          AND b.visibility = org.example.nextstepbackend.enums.Visibility.PRIVATE
          AND (
              b.createdBy.email = :email
              OR EXISTS (
                  SELECT 1 FROM BoardMember bm2
                  WHERE bm2.board = b
                    AND bm2.user.email = :email
                    AND bm2.role IN (
                        org.example.nextstepbackend.enums.BoardRole.OBSERVER,
                        org.example.nextstepbackend.enums.BoardRole.ADMIN,
                        org.example.nextstepbackend.enums.BoardRole.MEMBER
                    )
              )
          )
    """)
  Page<Board> findPrivateBoards(String slug, String email, Pageable pageable);

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
