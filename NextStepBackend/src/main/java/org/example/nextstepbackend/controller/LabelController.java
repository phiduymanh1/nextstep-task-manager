package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.BoardLabelRequest;
import org.example.nextstepbackend.dto.request.SelectedCardLabelRequest;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.label.LabelService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/labels")
public class LabelController extends BaseController {

  private final LabelService labelService;

  public LabelController(ApiResponseUtil responseUtil, LabelService labelService) {
    super(responseUtil);
    this.labelService = labelService;
  }

  @PostMapping("/{boardSlug}")
  public ResponseEntity<ApiResponse<Void>> createBoardLabel(
      @PathVariable String boardSlug, @Valid @RequestBody BoardLabelRequest request) {
    labelService.createBoardLabel(boardSlug,request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(success(MessageConst.LABEL_CREATE_SUCCESS, null));
  }

  @PostMapping("/selected")
  public ResponseEntity<ApiResponse<Void>> selectedLabel(@Valid @RequestBody SelectedCardLabelRequest request){

    labelService.selectedCardLabel(request);
    return ResponseEntity.ok(success(MessageConst.LABEL_SELECTED_SUCCESS,null));
  }
}
