package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.services.workspace.WorkSpaceService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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

  /** Api to create a new workspace */
  @PostMapping("/me")
  public ResponseEntity<ApiResponse<Void>> createWorkSpace(
      @Valid @RequestBody WorkSpaceRequest request) {
    workSpaceService.createWorkSpace(request);
    return ResponseEntity.ok(success(MessageConst.WORK_SPACE_CREATE_SUCCESS, null));
  }

  /** Api to get all workspaces of current user */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkSpaceOfUser(
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      throw new InvalidInputException("Unauthenticated");
    }
    List<WorkspaceResponse> workspaceResponses =
        workSpaceService.getWorkspaceMe(userDetails.getUsername());
    return ResponseEntity.ok(success(null, workspaceResponses));
  }
}
