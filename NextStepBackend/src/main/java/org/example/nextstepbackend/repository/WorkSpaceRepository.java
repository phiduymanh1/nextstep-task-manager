package org.example.nextstepbackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.nextstepbackend.entity.Workspace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkSpaceRepository extends JpaRepository<Workspace, Integer> {

  Workspace findBySlug(String slug);

  @EntityGraph(attributePaths = {"createdBy"})
  List<Workspace> findByCreatedBy_Email(String email);

  @EntityGraph(attributePaths = {"createdBy"})
  Optional<Workspace> findBySlugAndCreatedBy_Email(String slug, String email);

  @Query(
      """
    SELECT DISTINCT w FROM Workspace w
    LEFT JOIN FETCH w.members m
    LEFT JOIN FETCH m.user u
    WHERE w.slug = :slug
      AND (
        (w.visibility = 'PRIVATE' AND w.createdBy.email = :email)
        OR (w.visibility = 'WORKSPACE' AND (w.createdBy.email = :email OR u.email = :email))
        OR (w.visibility = 'PUBLIC')
      )
""")
  Optional<Workspace> findWorkspaceDetail(@Param("slug") String slug, @Param("email") String email);
}
