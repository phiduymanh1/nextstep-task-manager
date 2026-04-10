package org.example.nextstepbackend.controller;

import java.util.List;
import org.apache.coyote.BadRequestException;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.AddBoardMemberRequest;
import org.example.nextstepbackend.dto.request.BoardMemberResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.boardmember.BoardMemberService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board-member")
public class BoardMemberController extends BaseController {

  private final BoardMemberService boardMemberService;

  public BoardMemberController(
      ApiResponseUtil responseUtil, BoardMemberService boardMemberService) {
    super(responseUtil);
    this.boardMemberService = boardMemberService;
  }

  @GetMapping("/boards/{boardSlug}/members")
  public ResponseEntity<ApiResponse<List<BoardMemberResponse>>> getBoardMembers(
      @PathVariable String boardSlug) {

    List<BoardMemberResponse> response = boardMemberService.getBoardMembers(boardSlug);
    return ResponseEntity.ok(success(null, response));
  }

  @PostMapping("/boards/{boardSlug}/members")
  public ResponseEntity<ApiResponse<Void>> addMemberToBoard(
      @PathVariable String boardSlug, @RequestBody AddBoardMemberRequest request) {

    boardMemberService.addMemberToBoard(boardSlug, request.userId(), request.role().name());

    return ResponseEntity.ok(success(MessageConst.BOARD_ADD_MEMBER_SUCCESS, null));
  }

  @DeleteMapping("/boards/{boardSlug}/members/{userId}")
  public ResponseEntity<ApiResponse<Void>> removeMemberFromBoard(
      @PathVariable String boardSlug, @PathVariable Integer userId) throws BadRequestException {

    boardMemberService.removeMemberFromBoard(boardSlug, userId);
    return ResponseEntity.ok(success(MessageConst.BOARD__REMOVE_MEMBER_SUCCESS, null));
  }
}
