package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.CardPositionRequest;
import org.example.nextstepbackend.dto.request.CardRequest;
import org.example.nextstepbackend.dto.request.CardUpdateRequest;
import org.example.nextstepbackend.dto.response.card.CardResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.card.CardService;
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
@RequestMapping("/cards")
public class CardController extends BaseController {

  private final CardService cardService;

  public CardController(ApiResponseUtil responseUtil, CardService cardService) {
    super(responseUtil);
    this.cardService = cardService;
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
}
