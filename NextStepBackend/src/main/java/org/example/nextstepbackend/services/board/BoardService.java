package org.example.nextstepbackend.services.board;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.dto.request.BoardUpdateRequest;
import org.example.nextstepbackend.dto.response.board.BoardDetailResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.dto.response.lists.ListsResponse;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.entity.ListEntity;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.entity.WorkspaceMember;
import org.example.nextstepbackend.enums.BoardRole;
import org.example.nextstepbackend.enums.Visibility;
import org.example.nextstepbackend.exceptions.DuplicateResourceException;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.BoardMapper;
import org.example.nextstepbackend.mappers.ListMapper;
import org.example.nextstepbackend.repository.BoardMemberRepository;
import org.example.nextstepbackend.repository.BoardRepository;
import org.example.nextstepbackend.repository.ListsRepository;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.example.nextstepbackend.repository.WorkspaceMemberRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.list.PermissionService;
import org.example.nextstepbackend.utils.SlugUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private final RoleBoardService roleBoardService;

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
    WorkspaceMember workspaceMember =
        roleBoardService.getWorkspaceMember(workspace.getId(), user.getId());
    permissionService.checkCanUpdateWorkspace(workspaceMember.getRole());

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

    roleBoardService.checkRoleBoard(boardSlug, authService.getCurrentUserId(), Const.DELETE_MODE);

    boardRepository.delete(board);
  }

  /** Get board details by slug, including lists and cards with pagination */
  public BoardDetailResponse getBoardDetail(String boardSlug, int page, int size) {
    size = Math.min(size, 50);

    String email = authService.getCurrentUser().getEmail();

    Board board =
        boardRepository
            .findBoardAccessible(boardSlug, email)
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

    roleBoardService.checkRoleBoard(boardSlug, authService.getCurrentUserId(), Const.UPDATE_MODE);

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
}
