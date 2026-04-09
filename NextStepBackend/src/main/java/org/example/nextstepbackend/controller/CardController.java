package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.ActivityResponse;
import org.example.nextstepbackend.dto.request.CardDetailResponse;
import org.example.nextstepbackend.dto.request.CardPositionRequest;
import org.example.nextstepbackend.dto.request.CardRequest;
import org.example.nextstepbackend.dto.request.CardUpdateRequest;
import org.example.nextstepbackend.dto.request.CommentResponse;
import org.example.nextstepbackend.dto.response.card.CardResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.ActivityService;
import org.example.nextstepbackend.services.card.CardService;
import org.example.nextstepbackend.services.comment.CommentService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/cards")
public class CardController extends BaseController {

  private final CardService cardService;
  private final CommentService commentService;
  private final ActivityService activityService;

  public CardController(
      ApiResponseUtil responseUtil,
      CardService cardService,
      CommentService commentService,
      ActivityService activityService) {
    super(responseUtil);
    this.cardService = cardService;
    this.commentService = commentService;
    this.activityService = activityService;
  }

  /** API create card by listId */
  @PostMapping("/lists/{listId}/cards")
  public ResponseEntity<ApiResponse<CardResponse>> createCard(
      @PathVariable Integer listId, @Valid @RequestBody CardRequest request) {

    CardResponse response = cardService.createCard(listId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(success(MessageConst.CARD_CREATE_SUCCESS, response));
  }

  /** API archive card */
  @DeleteMapping("/{cardId}")
  public ResponseEntity<ApiResponse<Void>> archiveCard(@PathVariable Integer cardId) {

    cardService.archiveCard(cardId);

    return ResponseEntity.ok(success(MessageConst.CARD_ARCHIVE_SUCCESS, null));
  }

  /** API update card */
  @PatchMapping("/{cardId}")
  public ResponseEntity<ApiResponse<Void>> updateCard(
      @PathVariable Integer cardId, @Valid @RequestBody CardUpdateRequest request) {

    cardService.updateCard(cardId, request);

    return ResponseEntity.ok(success(MessageConst.BOARD_UPDATE_SUCCESS, null));
  }

  /** API update card position (reorder / move) */
  @PatchMapping("/{cardId}/position")
  public ResponseEntity<ApiResponse<Void>> updateCardPosition(
      @PathVariable Integer cardId, @Valid @RequestBody CardPositionRequest request) {

    cardService.updateCardPosition(cardId, request);

    return ResponseEntity.ok(success(MessageConst.BOARD_UPDATE_SUCCESS, null));
  }

  @GetMapping("/{cardId}/detail")
  public ResponseEntity<ApiResponse<CardDetailResponse>> getCardDetail(
      @PathVariable Integer cardId) {

    CardDetailResponse response = cardService.getCardDetail(cardId);
    return ResponseEntity.ok(success(null, response));
  }

  @GetMapping("/{id}/comments")
  public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
      @PathVariable Integer id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = Pageable.ofSize(size).withPage(page);
    PageResponse<CommentResponse> response = commentService.getComments(id, pageable);
    return ResponseEntity.ok(success(null, response));
  }

  @GetMapping("/{id}/activities")
  public ResponseEntity<ApiResponse<PageResponse<ActivityResponse>>> getActivities(
      @PathVariable Integer id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("audit.createdAt").descending());
    PageResponse<ActivityResponse> response = activityService.getActivities(id, pageable);
    return ResponseEntity.ok(success(null, response));
  }

  @PatchMapping("/{cardId}/list")
  public ResponseEntity<ApiResponse<Void>> moveCardToList(
      @PathVariable Integer cardId, @RequestParam Integer listId) {
    cardService.moveCardToList(cardId, listId);
    return ResponseEntity.ok(success(MessageConst.CARD_MOVE_SUCCESS, null));
  }
}
