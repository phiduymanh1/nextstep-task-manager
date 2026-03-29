package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.request.WorkSpaceUpdateRequest;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceDetailResponse;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.services.workspace.WorkSpaceService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

  /** Api to update workspace info by slug for user */
  @PatchMapping("/me/{slug}")
  public ResponseEntity<ApiResponse<Void>> updateWorkSpace(
      @PathVariable("slug") String slug,
      @RequestBody WorkSpaceUpdateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      throw new InvalidInputException("Unauthenticated");
    }
    workSpaceService.updateWorkspace(slug, userDetails.getUsername(), request);
    return ResponseEntity.ok(success(MessageConst.WORK_SPACE_UPDATE_SUCCESS, null));
  }

  /** Api to delete workspace by slug for user */
  @DeleteMapping("/me/{slug}")
  public ResponseEntity<ApiResponse<Void>> deleteWorkSpace(
      @PathVariable("slug") String slug, @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      throw new InvalidInputException("Unauthenticated");
    }
    workSpaceService.deleteWorkspace(slug, userDetails.getUsername());
    return ResponseEntity.ok(success(MessageConst.WORK_SPACE_DELETE_SUCCESS, null));
  }

  /** Api get workspace detail by slug */
  @GetMapping("/boards/{slug}")
  public ResponseEntity<ApiResponse<WorkspaceDetailResponse>> getWorkspaceDetail(
      @PathVariable String slug,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      throw new InvalidInputException("Unauthenticated");
    }

    var response = workSpaceService.getWorkspaceDetail(slug, userDetails.getUsername(), page, size);

    return ResponseEntity.ok(success(null, response));
  }
}
