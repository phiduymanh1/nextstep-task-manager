package org.example.nextstepbackend.repository;

import java.util.Optional;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> {

  Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);
}
