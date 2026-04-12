package org.example.nextstepbackend.services.boardmember;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.BoardMemberResponse;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.enums.BoardRole;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.InvalidTokenException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.repository.BoardMemberRepository;
import org.example.nextstepbackend.repository.BoardRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.board.RoleBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardMemberService {

  private final BoardMemberRepository boardMemberRepository;
  private final BoardRepository boardRepository;
  private final UserRepository userRepository;
  private final RoleBoardService roleBoardService;
  private final AuthService authService;

  @Transactional
  public List<BoardMemberResponse> getBoardMembers(String boardSlug) {

    List<BoardMember> members = boardMemberRepository.findByBoard_Slug(boardSlug);

    return members.stream()
        .map(
            m ->
                new BoardMemberResponse(
                    m.getUser().getId(),
                    m.getUser().getFullName(),
                    m.getUser().getAvatarUrl(),
                    m.getRole()))
        .toList();
  }

  @Transactional
  public void addMemberToBoard(String boardSlug, Integer userId, String role) {

    Board board =
        boardRepository
            .findBySlug(boardSlug)
            .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    roleBoardService.checkRoleBoard(boardSlug, authService.getCurrentUserId(), Const.CREATE_MODE);

    boolean exists = boardMemberRepository.existsByBoardAndUser(board, user);
    if (exists) {
      throw new ResourceNotFoundException("User already in board");
    }

    BoardMember boardMember = new BoardMember();
    boardMember.setBoard(board);
    boardMember.setUser(user);

    if (role == null) {
      role = "MEMBER";
    }

    try {
      boardMember.setRole(BoardRole.valueOf(role.toUpperCase()));
    } catch (Exception e) {
      throw new InvalidInputException("Invalid role");
    }

    boardMemberRepository.save(boardMember);
  }

  @Transactional
  public void removeMemberFromBoard(String boardSlug, Integer userId) throws BadRequestException {

    Board board =
        boardRepository
            .findBySlug(boardSlug)
            .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Integer currentUserId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(boardSlug, currentUserId, Const.DELETE_MODE);

    BoardMember boardMember =
        boardMemberRepository
            .findByBoardAndUser(board, user)
            .orElseThrow(() -> new ResourceNotFoundException("Member not in board"));

    if (userId.equals(currentUserId)) {
      throw new BadRequestException("You cannot remove yourself");
    }

    if (boardMember.getRole() == BoardRole.ADMIN) {
      long adminCount = boardMemberRepository.countByBoardAndRole(board, BoardRole.ADMIN);

      if (adminCount <= 1) {
        throw new BadRequestException("Cannot remove the last admin of the board");
      }
    }

    BoardMember currentUserMember =
        boardMemberRepository
            .findByBoardAndUser(board, userRepository.findById(currentUserId).get())
            .orElseThrow(() -> new ResourceNotFoundException("Current user not in board"));

    if (currentUserMember.getRole() == BoardRole.MEMBER
        && boardMember.getRole() == BoardRole.ADMIN) {
      throw new InvalidTokenException("You cannot remove an admin");
    }

    boardMemberRepository.delete(boardMember);
  }
}
