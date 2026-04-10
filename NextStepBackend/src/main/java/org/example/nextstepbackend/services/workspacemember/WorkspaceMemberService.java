package org.example.nextstepbackend.services.workspacemember;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.WorkspaceMemberResponse;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.example.nextstepbackend.enums.WorkspaceRole;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.example.nextstepbackend.repository.WorkspaceMemberRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.list.PermissionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final WorkSpaceRepository workSpaceRepository;
  private final UserRepository userRepository;
  private final PermissionService permissionService;
  private final AuthService authService;

  @Transactional
  public List<WorkspaceMemberResponse> getWorkspaceMember(String workspaceSlug) {

    List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspace_Slug(workspaceSlug);

    return members.stream()
        .map(
            m ->
                new WorkspaceMemberResponse(
                    m.getUser().getId(),
                    m.getUser().getFullName(),
                    m.getUser().getAvatarUrl(),
                    m.getRole()))
        .toList();
  }

  @Transactional
  public void addMember(String slug, Integer userId) {
    Workspace workspace = getWorkspaceOrThrow(slug);
    User user = getUserOrThrow(userId);

    WorkspaceMember currentMember = getCurrentMemberOrThrow(workspace);
    permissionService.checkCanUpdateWorkspace(currentMember.getRole());

    if (workspaceMemberRepository.existsByWorkspaceAndUser(workspace, user)) {
      throw new IllegalStateException("User already in workspace");
    }

    WorkspaceMember wm =
        WorkspaceMember.builder()
            .workspace(workspace)
            .user(user)
            .role(WorkspaceRole.MEMBER)
            .build();

    workspaceMemberRepository.save(wm);
  }

  @Transactional
  public void removeMember(String slug, Integer userId) {
    Workspace workspace = getWorkspaceOrThrow(slug);
    User user = getUserOrThrow(userId);

    WorkspaceMember currentMember = getCurrentMemberOrThrow(workspace);
    permissionService.checkCanDeleteWorkspace(currentMember.getRole());

    WorkspaceMember target =
        workspaceMemberRepository
            .findByWorkspace_IdAndUser_Id(workspace.getId(), user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

    workspaceMemberRepository.delete(target);
  }

  private WorkspaceMember getWorkspaceMember(Integer workspaceId, Integer userId) {
    return workspaceMemberRepository
        .findByWorkspace_IdAndUser_Id(workspaceId, userId)
        .orElseThrow(() -> new AccessDeniedException("You are not in this workspace"));
  }

  private Workspace getWorkspaceOrThrow(String slug) {
    Workspace workspace = workSpaceRepository.findBySlug(slug);
    if (workspace == null) {
      throw new ResourceNotFoundException("Workspace not found");
    }
    return workspace;
  }

  private User getUserOrThrow(Integer userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private WorkspaceMember getCurrentMemberOrThrow(Workspace workspace) {
    return getWorkspaceMember(workspace.getId(), authService.getCurrentUserId());
  }
}
