package org.example.nextstepbackend.services.board;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.dto.request.BoardUpdateRequest;
import org.example.nextstepbackend.dto.response.board.BoardDetailResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.dto.response.lists.ListsResponse;
import org.example.nextstepbackend.entity.*;
import org.example.nextstepbackend.enums.BoardRole;
import org.example.nextstepbackend.enums.Visibility;
import org.example.nextstepbackend.exceptions.DuplicateResourceException;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.InvalidTokenException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.BoardMapper;
import org.example.nextstepbackend.mappers.ListMapper;
import org.example.nextstepbackend.repository.*;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.list.PermissionService;
import org.example.nextstepbackend.utils.SlugUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

  private final AuthService authService;
  private final BoardRepository boardRepository;
  private final BoardMapper boardMapper;
  private final WorkSpaceRepository workSpaceRepository;
  private final ListsRepository listsRepository;
  private final ListMapper listMapper;
  private final BoardMemberRepository boardMemberRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final PermissionService permissionService;

  private static final String BOARD_FOUND = "Board not found";
  private static final String DELETE_MODE = "DELETE";
  private static final String CREATE_MODE = "CREATE";
  private static final String UPDATE_MODE = "UPDATE";

  /** Create a new board within a workspace */
  @Transactional
  public void createBoardByWorkspace(String wSpaceSlug, BoardRequest request) {
    // Map the request to a Board entity
    Board board = boardMapper.toEntity(request);
    User user = authService.getCurrentUser();
    // Resolve workspace by slug (must exist)
    Workspace workspace = workSpaceRepository.findBySlug(wSpaceSlug);

    if (workspace == null) {
      throw new ResourceNotFoundException("Workspace not found");
    }


    checkRoleBoard(null, user.getId(), workspace.getId(), CREATE_MODE);

    String baseSlug = SlugUtils.toSlug(board.getName() + Const.HYPHEN + user.getUsername());
    String slug = generateUniqueSlug(baseSlug, workspace);

    board.setCreatedBy(user);
    board.setSlug(slug);
    if (board.getVisibility() == null) {
      board.setVisibility(Visibility.WORKSPACE);
    }

    BoardMember boardMember = new BoardMember();
    boardMember.setRole(BoardRole.ADMIN);

    board.addMember(boardMember);
    user.addBoardMembership(boardMember);

    board.setWorkspace(workspace);

    // Save the board to the database
    try {
      boardRepository.save(board);
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateResourceException("Slug duplicate");
    }
  }

  /** Generate a unique slug for the board within the workspace */
  private String generateUniqueSlug(String baseSlug, Workspace workspace) {
    String slug = baseSlug;
    int counter = 1;

    while (boardRepository.existsByWorkspaceAndSlug(workspace, slug)) {
      slug = baseSlug + "-" + counter;
      counter++;
    }

    return slug;
  }

  /** Close a board by slug within a workspace */
  @Transactional
  public void closeBoardBySlug(String workspaceSlug, String boardSlug) {
    Board board =
        boardRepository
            .findByWorkspace_SlugAndSlug(workspaceSlug, boardSlug)
            .orElseThrow(() -> new ResourceNotFoundException(BOARD_FOUND));

    checkRoleBoard(boardSlug, authService.getCurrentUserId(), board.getWorkspace().getId(), DELETE_MODE);

    boardRepository.delete(board);
  }

  /** Get board details by slug, including lists and cards with pagination */
  public BoardDetailResponse getBoardDetail(String boardSlug, int page, int size) {
    size = Math.min(size, 50);

    String email = authService.getCurrentUser().getEmail();

    Board board =
        boardRepository
            .findBoardBySlugAndMember(boardSlug, email)
            .orElseThrow(() -> new ResourceNotFoundException(BOARD_FOUND));

    Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());

    Page<ListEntity> listPage = listsRepository.findByBoardSlug(boardSlug, pageable);

    PageResponse<ListsResponse> lists = toPageResponse(listPage.map(listMapper::toResponse));

    return new BoardDetailResponse(
        board.getId(),
        board.getName(),
        board.getSlug(),
        board.getBackgroundColor(),
        board.getBackgroundImageUrl(),
        board.getVisibility(),
        lists);
  }

  /** Helper method to convert Page of ListsResponse to PageResponse */
  public PageResponse<ListsResponse> toPageResponse(Page<ListsResponse> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  @Transactional
  public void updateBoard(String boardSlug, BoardUpdateRequest request) {
    Board board =
        boardRepository
            .findBySlug(boardSlug)
            .orElseThrow(() -> new ResourceNotFoundException(BOARD_FOUND));

    checkRoleBoard(boardSlug, authService.getCurrentUserId(), board.getWorkspace().getId(), UPDATE_MODE);
    boolean updated = false;

    if (request.name() != null) {
      board.setName(request.name());
      updated = true;
    }

    if (request.description() != null) {
      board.setDescription(request.description());
      updated = true;
    }

    if (request.backgroundColor() != null) {
      board.setBackgroundColor(request.backgroundColor());
      updated = true;
    }

    if (request.backgroundImageUrl() != null) {
      board.setBackgroundImageUrl(request.backgroundImageUrl());
      updated = true;
    }

    if (request.visibility() != null) {
      board.setVisibility(request.visibility());
      updated = true;
    }

    if (!updated) {
      throw new InvalidInputException("No fields to update");
    }
  }

  private void checkRoleBoard(String boardSlug, Integer userId, Integer workspaceId, String mode) {
    BoardMember BoardMember = getBoardMember(boardSlug, userId);
    WorkspaceMember workspaceMember = getWorkspaceMember(workspaceId, userId);

    if (mode.equals(DELETE_MODE)) {
      permissionService.checkCanDelete(workspaceMember, BoardMember);
    }else if (mode.equals(UPDATE_MODE)){
      permissionService.checkCanEdit(workspaceMember, BoardMember);
    }else if (mode.equals(CREATE_MODE)){
      permissionService.checkCanUpdateWorkspace(workspaceMember.getRole());
    }

  }

  private BoardMember getBoardMember(String slug, Integer userId) {
    return boardMemberRepository
            .findByBoard_SlugAndUser_Id(slug, userId)
            .orElseThrow(() -> new AccessDeniedException("You are not in this board"));
  }

  private WorkspaceMember getWorkspaceMember(Integer workspaceId, Integer userId) {
    return workspaceMemberRepository
            .findByWorkspace_IdAndUser_Id(workspaceId, userId)
            .orElseThrow(() -> new AccessDeniedException("You are not in this workspace"));
  }
}
