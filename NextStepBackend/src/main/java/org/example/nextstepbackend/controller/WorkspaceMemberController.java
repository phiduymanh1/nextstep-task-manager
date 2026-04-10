package org.example.nextstepbackend.controller;

import java.util.List;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.WorkspaceMemberResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.services.workspacemember.WorkspaceMemberService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspace-member")
public class WorkspaceMemberController extends BaseController {

  private final WorkspaceMemberService workspaceMemberService;

  public WorkspaceMemberController(
      ApiResponseUtil responseUtil, WorkspaceMemberService workspaceMemberService) {
    super(responseUtil);
    this.workspaceMemberService = workspaceMemberService;
  }

  @GetMapping("/{workspaceSlug}/members")
  public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getBoardMembers(
      @PathVariable String workspaceSlug) {

    List<WorkspaceMemberResponse> response =
        workspaceMemberService.getWorkspaceMember(workspaceSlug);
    return ResponseEntity.ok(success(null, response));
  }

  @PostMapping("/workspaces/{slug}/members")
  public ResponseEntity<ApiResponse<Void>> addMember(
      @PathVariable String slug, @RequestParam Integer userId) {
    workspaceMemberService.addMember(slug, userId);
    return ResponseEntity.ok(success(null, null));
  }

  @DeleteMapping("/workspaces/{slug}/members/{userId}")
  public ResponseEntity<ApiResponse<Void>> removeMember(
      @PathVariable String slug, @PathVariable Integer userId) {
    workspaceMemberService.removeMember(slug, userId);
    return ResponseEntity.ok(success(null, null));
  }
}
