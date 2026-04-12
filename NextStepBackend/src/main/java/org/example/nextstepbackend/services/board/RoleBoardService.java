package org.example.nextstepbackend.services.board;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.NotPermissionException;
import org.example.nextstepbackend.repository.BoardMemberRepository;
import org.example.nextstepbackend.repository.WorkspaceMemberRepository;
import org.example.nextstepbackend.services.list.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleBoardService {

  private final PermissionService permissionService;
  private final BoardMemberRepository boardMemberRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;

  @Transactional
  public void checkRoleBoard(String boardSlug, Integer userId, String mode) {

    BoardMember boardMember = getBoardMember(boardSlug, userId);

    Integer wsId = boardMember.getBoard().getWorkspace().getId();

    WorkspaceMember workspaceMember = getWorkspaceMember(wsId, userId);

    switch (mode) {
      case Const.DELETE_MODE -> permissionService.checkCanDelete(workspaceMember, boardMember);

      case Const.UPDATE_MODE -> permissionService.checkCanEdit(workspaceMember, boardMember);

      case Const.CREATE_MODE ->
          permissionService.checkCanUpdateWorkspace(workspaceMember.getRole());

      default -> throw new InvalidInputException("Invalid mode: " + mode);
    }
  }

  private BoardMember getBoardMember(String slug, Integer userId) {
    return boardMemberRepository
        .findByBoard_SlugAndUser_Id(slug, userId)
        .orElseThrow(() -> new NotPermissionException("You are not in this board"));
  }

  public WorkspaceMember getWorkspaceMember(Integer workspaceId, Integer userId) {
    return workspaceMemberRepository
        .findByWorkspace_IdAndUser_Id(workspaceId, userId)
        .orElseThrow(() -> new NotPermissionException("You are not in this workspace"));
  }
}
