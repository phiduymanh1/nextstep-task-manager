package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.ListPositionRequest;
import org.example.nextstepbackend.dto.request.ListsRequest;
import org.example.nextstepbackend.dto.request.ListsUpdateRequest;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.list.ListService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lists")
public class ListController extends BaseController {

  private final ListService listService;

  public ListController(ApiResponseUtil responseUtil, ListService listService) {
    super(responseUtil);
    this.listService = listService;
  }

  /** Api create list by slug of board */
  @PostMapping("/board/{boardSlug}")
  public ResponseEntity<ApiResponse<Void>> createList(
      @PathVariable String boardSlug, @Valid @RequestBody ListsRequest request) {
    listService.createListByBoardSlug(boardSlug, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(success(MessageConst.LIST_CREATE_SUCCESS, null));
  }

  @DeleteMapping("/{boardSlug}/{listId}")
  public ResponseEntity<ApiResponse<Void>> archiveList(
      @PathVariable String boardSlug, @PathVariable Integer listId) {
    listService.archiveList(boardSlug, listId);
    return ResponseEntity.ok(success(MessageConst.LIST_ARCHIVE_SUCCESS, null));
  }

  @PatchMapping("/{boardSlug}/{listId}")
  public ResponseEntity<ApiResponse<Void>> updateList(
      @PathVariable String boardSlug,
      @PathVariable Integer listId,
      @Valid @RequestBody ListsUpdateRequest request) {
    listService.updateList(boardSlug, listId, request);
    return ResponseEntity.ok(success(MessageConst.LIST_UPDATE_SUCCESS, null));
  }

  @PatchMapping("/{boardSlug}/{listId}/position")
  public ResponseEntity<ApiResponse<Void>> updateListPosition(
      @PathVariable String boardSlug,
      @PathVariable Integer listId,
      @Valid @RequestBody ListPositionRequest request) {

    listService.updateListPosition(boardSlug, listId, request);
    return ResponseEntity.ok(success(MessageConst.LIST_UPDATE_SUCCESS, null));
  }
}
