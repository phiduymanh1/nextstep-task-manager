package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.board.BoardService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
public class BoardController extends BaseController {

  private final BoardService boardService;

  public BoardController(ApiResponseUtil responseUtil, BoardService boardService) {
    super(responseUtil);
    this.boardService = boardService;
  }

  @PostMapping("/{wSpaceSlug}")
  public ResponseEntity<ApiResponse<Void>> createBoardByWorkspace(
      @PathVariable("wSpaceSlug") String wSpaceSlug, @Valid @RequestBody BoardRequest request) {
    boardService.createBoardByWorkspace(wSpaceSlug, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(success(MessageConst.BOARD_CREATE_SUCCESS, null));
  }
}
