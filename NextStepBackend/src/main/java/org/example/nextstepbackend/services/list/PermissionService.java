package org.example.nextstepbackend.services.list;

import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.example.nextstepbackend.enums.BoardRole;
import org.example.nextstepbackend.enums.WorkspaceRole;
import org.example.nextstepbackend.exceptions.NotPermissionException;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

  public void checkCanEdit(WorkspaceMember ws, BoardMember bd) {

    if (ws.getRole() == WorkspaceRole.GUEST) {
      throw new NotPermissionException("Workspace guest cannot modify");
    }

    if (bd.getRole() == BoardRole.OBSERVER) {
      throw new NotPermissionException("Board observer cannot modify");
    }
  }

  public void checkCanDelete(WorkspaceMember ws, BoardMember bd) {

    checkCanEdit(ws, bd);
  }

  public void checkCanUpdateWorkspace(WorkspaceRole role) {

    if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
      throw new NotPermissionException("No permission to update workspace");
    }
  }

  public void checkCanDeleteWorkspace(WorkspaceRole role) {

    if (role != WorkspaceRole.OWNER) {
      throw new NotPermissionException("Only owner can delete workspace");
    }
  }
}
