package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.CommentRequest;
import org.example.nextstepbackend.dto.request.CommentResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.services.comment.CommentService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
public class CommentController extends BaseController {

  private final CommentService commentService;

  public CommentController(ApiResponseUtil responseUtil, CommentService commentService) {
    super(responseUtil);
    this.commentService = commentService;
  }

  @PostMapping("/{cardId}")
  public ResponseEntity<ApiResponse<CommentResponse>> commentCard(
      @PathVariable Integer cardId, @Valid @RequestBody CommentRequest request) {

    CommentResponse res = commentService.commentCard(cardId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(success(null, res));
  }
}
