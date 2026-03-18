package org.example.nextstepbackend.services.workspace;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceResponse;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.exceptions.DuplicateResourceException;
import org.example.nextstepbackend.mappers.WorkSpaceMapper;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.utils.SlugUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkSpaceService {

  private final WorkSpaceMapper workSpaceMapper;
  private final AuthService authService;
  private final WorkSpaceRepository workSpaceRepository;

  /** Create a new workspace */
  public void createWorkSpace(WorkSpaceRequest request) {
    // Generate slug from workspace name and current user's username
    Workspace workspace = workSpaceMapper.toWorkspace(request);
    User user = authService.getCurrentUser();
    String slug = SlugUtils.toSlug(workspace.getName() + Const.HYPHEN + user.getUsername());
    if (validateDuplicateSlug(slug)) {
      throw new DuplicateResourceException(ValidateMessageConst.SLUG_DUPLICATE);
    }

    workspace.setCreatedBy(user);
    workspace.setSlug(slug);

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
}
