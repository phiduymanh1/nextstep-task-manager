package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.Workspace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkSpaceRepository extends JpaRepository<Workspace, Integer> {

  Workspace findBySlug(String slug);

  @EntityGraph(attributePaths = {"createdBy"})
  List<Workspace> findByCreatedBy_Email(String email);
}
