package org.example.nextstepbackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkSpaceRepository extends JpaRepository<Workspace, Integer> {

  Workspace findBySlug(String slug);

  Optional<Workspace> findWsBySlug(String slug);

  @EntityGraph(attributePaths = {"createdBy"})
  @Query(
      """
    SELECT wm.workspace
    FROM WorkspaceMember wm
    WHERE wm.user.email = :email
""")
  List<Workspace> findWorkspacesByUserEmail(String email);

  @EntityGraph(attributePaths = {"createdBy"})
  Optional<Workspace> findBySlugAndCreatedBy_Email(String slug, String email);

  @Query(
      """
    SELECT DISTINCT w FROM Workspace w
    LEFT JOIN FETCH w.members m
    LEFT JOIN FETCH m.user u
    WHERE w.slug = :slug
    AND (

        (
            w.visibility = 'PRIVATE'
            AND (
                w.createdBy.email = :email
                OR EXISTS (
                    SELECT 1 FROM WorkspaceMember wm
                    JOIN wm.user wu
                    WHERE wm.workspace.id = w.id
                    AND wu.email = :email
                    AND wm.role IN :roles
                )
                OR EXISTS (
                    SELECT 1 FROM Board b
                    JOIN BoardMember bm ON bm.board.id = b.id
                    JOIN bm.user bu
                    WHERE b.workspace.id = w.id
                    AND bu.email = :email
                )
            )
        )


        OR (
            w.visibility = 'WORKSPACE'
            AND (
                w.createdBy.email = :email
                OR (
                    u.email = :email
                    AND m.role IN :roles
                )
            )
        )


        OR (w.visibility = 'PUBLIC')
    )
    """)
  Optional<Workspace> findWorkspaceAccessible(String slug, String email, List<WorkspaceRole> roles);
}
