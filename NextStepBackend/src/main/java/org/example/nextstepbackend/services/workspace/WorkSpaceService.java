package org.example.nextstepbackend.services.workspace;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.request.WorkSpaceUpdateRequest;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceResponse;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.example.nextstepbackend.enums.WorkspaceRole;
import org.example.nextstepbackend.exceptions.DuplicateResourceException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.WorkSpaceMapper;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.utils.SlugUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkSpaceService {

  private final WorkSpaceMapper workSpaceMapper;
  private final AuthService authService;
  private final WorkSpaceRepository workSpaceRepository;

  /** Create a new workspace */
  @Transactional
  public void createWorkSpace(WorkSpaceRequest request) {
    // Initialize workspace entity from request
    Workspace workspace = workSpaceMapper.toWorkspace(request);
    User user = authService.getCurrentUser();
    String slug = SlugUtils.toSlug(workspace.getName() + Const.HYPHEN + user.getUsername());
    if (validateDuplicateSlug(slug)) {
      throw new DuplicateResourceException(ValidateMessageConst.SLUG_DUPLICATE);
    }

    workspace.setCreatedBy(user);
    workspace.setSlug(slug);

    // Create record membership for current user as owner of workspace
    WorkspaceMember membership = new WorkspaceMember();
    membership.setRole(WorkspaceRole.OWNER);

    // Used helper method
    workspace.addMember(membership); // relation member with workspace
    user.addWorkspaceMembership(membership); // relation member with user

    workSpaceRepository.save(workspace);
  }

  /** Validate if the generated slug is duplicate */
  public boolean validateDuplicateSlug(String slug) {
    return workSpaceRepository.findBySlug(slug) != null;
  }

  /** Get current user's workspace info */
  public List<WorkspaceResponse> getWorkspaceMe(String email) {
    List<Workspace> workspaces = workSpaceRepository.findByCreatedBy_Email(email);

    return workSpaceMapper.toWorkspaceResponseList(workspaces);
  }

  /** Update workspace info by slug or current user */
  public void updateWorkspace(String slug, String email, WorkSpaceUpdateRequest request) {
    // Find workspace by slug and current user's email
    Workspace workspace = getWorkspaceBySlugAndCurrentUser(slug, email);

    if (request.name() != null) {
      workspace.setName(request.name());
    }

    if (request.description() != null) {
      workspace.setDescription(request.description());
    }

    if (request.visibility() != null) {
      workspace.setVisibility(request.visibility());
    }

    // Slug not update when workspace name updated
    workSpaceRepository.save(workspace);
  }

  /** Delete workspace by slug and current user's email */
  public void deleteWorkspace(String slug, String email) {
    Workspace workspace = getWorkspaceBySlugAndCurrentUser(slug, email);
    workSpaceRepository.delete(workspace);
  }

  /** get workspace by slug and current user's email */
  public Workspace getWorkspaceBySlugAndCurrentUser(String slug, String email) {
    return workSpaceRepository
        .findBySlugAndCreatedBy_Email(slug, email)
        .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
  }
}
