package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.dto.request.BoardUpdateRequest;
import org.example.nextstepbackend.dto.response.board.BoardDetailResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.board.BoardService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
public class BoardController extends BaseController {

  private final BoardService boardService;

  public BoardController(ApiResponseUtil responseUtil, BoardService boardService) {
    super(responseUtil);
    this.boardService = boardService;
  }

  /** Create a new board within a workspace */
  @PostMapping("/{wSpaceSlug}")
  public ResponseEntity<ApiResponse<Void>> createBoardByWorkspace(
      @PathVariable("wSpaceSlug") String wSpaceSlug, @Valid @RequestBody BoardRequest request) {
    boardService.createBoardByWorkspace(wSpaceSlug, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(success(MessageConst.BOARD_CREATE_SUCCESS, null));
  }

  /** Close a board by slug within a workspace */
  @DeleteMapping("/{wSpaceSlug}/{boardSlug}")
  public ResponseEntity<ApiResponse<Void>> closeBoardBySlug(
      @PathVariable("wSpaceSlug") String wSpaceSlug, @PathVariable("boardSlug") String boardSlug) {
    boardService.closeBoardBySlug(wSpaceSlug, boardSlug);
    return ResponseEntity.ok(success(MessageConst.BOARD_DELETE_SUCCESS, null));
  }

  /** Api get list by slug of board */
  @GetMapping("/{slug}")
  public ResponseEntity<ApiResponse<BoardDetailResponse>> getBoardDetail(
      @PathVariable String slug,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    var response = boardService.getBoardDetail(slug, page, size);
    return ResponseEntity.ok((success(null, response)));
  }

  @PatchMapping("/{slug}")
  public ResponseEntity<ApiResponse<Void>> updateBoard(
      @PathVariable String slug, @Valid @RequestBody BoardUpdateRequest request) {
    boardService.updateBoard(slug, request);

    return ResponseEntity.ok(success(MessageConst.BOARD_UPDATE_SUCCESS, null));
  }
}
