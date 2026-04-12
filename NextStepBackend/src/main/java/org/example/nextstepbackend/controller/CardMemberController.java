package org.example.nextstepbackend.controller;

import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.services.cardmember.CardMemberService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card-member")
public class CardMemberController extends BaseController {

  private final CardMemberService cardMemberService;

  public CardMemberController(ApiResponseUtil responseUtil, CardMemberService cardMemberService) {
    super(responseUtil);
    this.cardMemberService = cardMemberService;
  }

  // Api assign member to card
  @PatchMapping("cards/{cardId}/assign")
  public ResponseEntity<ApiResponse<Void>> assignMemberToCard(
      @PathVariable Integer cardId, @RequestParam Integer userId) {
    cardMemberService.assignMember(cardId, userId);
    return ResponseEntity.ok(success(null, null));
  }

  // Api unassign member from card
  @PatchMapping("cards/{cardId}/unassign")
  public ResponseEntity<ApiResponse<Void>> unassignMemberFromCard(
      @PathVariable Integer cardId, @RequestParam Integer userId) {

    cardMemberService.unAssignMember(cardId, userId);
    return ResponseEntity.ok(success(null, null));
  }
}
