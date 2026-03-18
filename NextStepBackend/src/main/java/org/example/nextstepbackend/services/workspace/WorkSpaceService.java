package org.example.nextstepbackend.services.workspace;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
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

  public void createWorkSpace(WorkSpaceRequest request) {

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

  public boolean validateDuplicateSlug(String slug) {
    return workSpaceRepository.findBySlug(slug) != null;
  }
}
