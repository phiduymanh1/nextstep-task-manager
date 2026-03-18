package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.workspace.WorkSpaceService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/work-space")
public class WorkSpaceController extends BaseController {

  private WorkSpaceService workSpaceService;

  public WorkSpaceController(ApiResponseUtil responseUtil, WorkSpaceService workSpaceService) {
    super(responseUtil);
    this.workSpaceService = workSpaceService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createWorkSpace(
      @Valid @RequestBody WorkSpaceRequest request) {
    workSpaceService.createWorkSpace(request);
    return ResponseEntity.ok(success(MessageConst.WORK_SPACE_CREATE_SUCCESS, null));
  }
}
