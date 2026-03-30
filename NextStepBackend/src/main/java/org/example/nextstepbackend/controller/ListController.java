package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.ListsRequest;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.list.ListService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @PostMapping("/board/{boardSlug}")
  public ResponseEntity<ApiResponse<Void>> createList(
      @PathVariable String boardSlug, @Valid @RequestBody ListsRequest request) {
    listService.createListByBoardSlug(boardSlug, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(success(MessageConst.LIST_CREATE_SUCCESS, null));
  }
}
