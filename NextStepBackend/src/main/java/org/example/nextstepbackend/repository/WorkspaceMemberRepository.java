package org.example.nextstepbackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> {

  Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);

  List<WorkspaceMember> findByWorkspace_Slug(String slug);

  boolean existsByWorkspaceAndUser(Workspace workspace, User user);

  void deleteByWorkspaceAndUser(Workspace workspace, User user);

  Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);
}
