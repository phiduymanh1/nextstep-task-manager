package org.example.nextstepbackend.controller;

import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.BoardMemberResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.services.boardmember.BoardMemberService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/board-member")
public class BoardMemberController extends BaseController {

    private final BoardMemberService boardMemberService;

    public BoardMemberController(ApiResponseUtil responseUtil, BoardMemberService boardMemberService) {
        super(responseUtil);
        this.boardMemberService = boardMemberService;
    }

    @GetMapping("/boards/{boardSlug}/members")
    public ResponseEntity<ApiResponse<List<BoardMemberResponse>>> getBoardMembers(
            @PathVariable String boardSlug) {

        List<BoardMemberResponse> response = boardMemberService.getBoardMembers(boardSlug);
        return ResponseEntity.ok(success(null, response));
    }
}
