package org.example.nextstepbackend.services.cardmember;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.CardMember;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.enums.ActionType;
import org.example.nextstepbackend.enums.EntityType;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.repository.ActivityRepository;
import org.example.nextstepbackend.repository.CardMemberRepository;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.ActivityService;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.board.RoleBoardService;
import org.example.nextstepbackend.services.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardMemberService {

  private final CardMemberRepository cardMemberRepository;
  private final UserRepository userRepository;
  private final CardRepository cardRepository;
  private final RoleBoardService roleBoardService;
  private final AuthService authService;
  private final ActivityService activityService;
  private final UserService userService;
  private final ActivityRepository activityRepository;

  @Transactional
  public void assignMember(Integer cardId, Integer userId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Card card =
        cardRepository
            .findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

    Integer userCurrentId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(
        card.getList().getBoard().getSlug(), userCurrentId, Const.UPDATE_MODE);

    CardMember cardMember = new CardMember();
    cardMember.setCard(card);
    cardMember.setUser(user);

    cardMemberRepository.save(cardMember);

    activityService.logAddMemberToCard(card, userRepository.getReferenceById(userCurrentId), user);
  }

  @Transactional
  public void unAssignMember(Integer cardId, Integer userId) {

    userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Card card =
        cardRepository
            .findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

    Integer userCurrentId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(
        card.getList().getBoard().getSlug(), userCurrentId, Const.UPDATE_MODE);

    cardMemberRepository.deleteByUser_IdAndCard_Id(userId, cardId);

    activityRepository.deleteByCard_IdAndEntityTypeAndEntityIdAndActionType(
        card.getId(), EntityType.USER, userId, ActionType.ADD_MEMBER_TO_CARD);
  }
}
