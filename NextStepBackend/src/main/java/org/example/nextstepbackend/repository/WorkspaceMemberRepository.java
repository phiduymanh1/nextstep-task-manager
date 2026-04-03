package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> {

  Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);
}
