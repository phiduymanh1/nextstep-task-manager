package org.example.nextstepbackend.services.board;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.enums.Visibility;
import org.example.nextstepbackend.exceptions.DuplicateResourceException;
import org.example.nextstepbackend.exceptions.InvalidTokenException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.BoardMapper;
import org.example.nextstepbackend.repository.BoardRepository;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.utils.SlugUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

  private final AuthService authService;
  private final BoardRepository boardRepository;
  private final BoardMapper boardMapper;
  private final WorkSpaceRepository workSpaceRepository;

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

    String baseSlug = SlugUtils.toSlug(board.getName() + Const.HYPHEN + user.getUsername());
    String slug = generateUniqueSlug(baseSlug, workspace);

    board.setCreatedBy(user);
    board.setSlug(slug);
    if (board.getVisibility() == null) {
      board.setVisibility(Visibility.WORKSPACE);
    }

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
    Board board = boardRepository
            .findByWorkspace_SlugAndSlug(workspaceSlug, boardSlug)
            .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

    String emailCurrentUser = authService.getCurrentUser().getEmail();

    if (!board.getWorkspace().getCreatedBy().getEmail().equals(emailCurrentUser)) {
      throw new InvalidTokenException("You don't have permission");
    }

    boardRepository.delete(board);
  }
}
