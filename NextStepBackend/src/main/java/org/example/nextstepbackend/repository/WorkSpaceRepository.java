package org.example.nextstepbackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.nextstepbackend.entity.Workspace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkSpaceRepository extends JpaRepository<Workspace, Integer> {

  Workspace findBySlug(String slug);

  @EntityGraph(attributePaths = {"createdBy"})
  List<Workspace> findByCreatedBy_Email(String email);

  @EntityGraph(attributePaths = {"createdBy"})
  Optional<Workspace> findBySlugAndCreatedBy_Email(String slug, String email);
}
