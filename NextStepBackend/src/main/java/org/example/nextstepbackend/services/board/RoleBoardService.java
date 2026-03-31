package org.example.nextstepbackend.services.board;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.example.nextstepbackend.repository.BoardMemberRepository;
import org.example.nextstepbackend.repository.WorkspaceMemberRepository;
import org.example.nextstepbackend.services.list.PermissionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleBoardService {

  private static final String DELETE_MODE = "DELETE";
  private static final String CREATE_MODE = "CREATE";
  private static final String UPDATE_MODE = "UPDATE";
  private final PermissionService permissionService;
  private final BoardMemberRepository boardMemberRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;

  @Transactional
  public void checkRoleBoard(String boardSlug, Integer userId, Integer workspaceId, String mode) {

    BoardMember boardMember = getBoardMember(boardSlug, userId);

    Integer wsId =
            (mode.equals(CREATE_MODE)) ? boardMember.getBoard().getWorkspace().getId() : workspaceId;

    WorkspaceMember workspaceMember = getWorkspaceMember(wsId, userId);

    switch (mode) {
      case DELETE_MODE ->
              permissionService.checkCanDelete(workspaceMember, boardMember);

      case UPDATE_MODE ->
              permissionService.checkCanEdit(workspaceMember, boardMember);

      case CREATE_MODE ->
              permissionService.checkCanUpdateWorkspace(workspaceMember.getRole());

      default -> throw new IllegalArgumentException("Invalid mode: " + mode);
    }
  }

  private BoardMember getBoardMember(String slug, Integer userId) {
    return boardMemberRepository
        .findByBoard_SlugAndUser_Id(slug, userId)
        .orElseThrow(() -> new AccessDeniedException("You are not in this board"));
  }

  public WorkspaceMember getWorkspaceMember(Integer workspaceId, Integer userId) {
    return workspaceMemberRepository
        .findByWorkspace_IdAndUser_Id(workspaceId, userId)
        .orElseThrow(() -> new AccessDeniedException("You are not in this workspace"));
  }
}
