package org.example.nextstepbackend.services.card;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.AttachmentResponse;
import org.example.nextstepbackend.dto.request.CardDetailResponse;
import org.example.nextstepbackend.dto.request.CardPositionRequest;
import org.example.nextstepbackend.dto.request.CardRequest;
import org.example.nextstepbackend.dto.request.CardUpdateRequest;
import org.example.nextstepbackend.dto.request.ChecklistResponse;
import org.example.nextstepbackend.dto.request.LabelGroupResponse;
import org.example.nextstepbackend.dto.response.card.CardResponse;
import org.example.nextstepbackend.entity.Attachment;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.CardLabel;
import org.example.nextstepbackend.entity.CardMember;
import org.example.nextstepbackend.entity.Checklist;
import org.example.nextstepbackend.entity.ChecklistItem;
import org.example.nextstepbackend.entity.Label;
import org.example.nextstepbackend.entity.ListEntity;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.AttachmentMapper;
import org.example.nextstepbackend.mappers.CardMapper;
import org.example.nextstepbackend.mappers.ChecklistMapper;
import org.example.nextstepbackend.mappers.LabelGroupMapper;
import org.example.nextstepbackend.repository.AttachmentRepository;
import org.example.nextstepbackend.repository.CardLabelRepository;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.ChecklistItemRepository;
import org.example.nextstepbackend.repository.ChecklistRepository;
import org.example.nextstepbackend.repository.LabelRepository;
import org.example.nextstepbackend.repository.ListsRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.ActivityService;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.board.RoleBoardService;
import org.example.nextstepbackend.utils.PositionUtils;
import org.example.nextstepbackend.utils.RebalanceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

  private final AuthService authService;
  private final ListsRepository listsRepository;
  private final RoleBoardService roleBoardService;
  private final UserRepository userRepository;
  private final CardRepository cardRepository;
  private final CardMapper cardMapper;
  private final LabelRepository labelRepository;
  private final CardLabelRepository cardLabelRepository;
  private final ChecklistRepository checklistRepository;
  private final ChecklistItemRepository checklistItemRepository;
  private final AttachmentRepository attachmentRepository;
  private final LabelGroupMapper labelGroupMapper;
  private final ChecklistMapper checklistMapper;
  private final AttachmentMapper attachmentMapper;
  private final ActivityService activityService;

  @Transactional
  public CardResponse createCard(Integer listId, CardRequest request) {

    Integer userId = authService.getCurrentUserId();

    ListEntity list =
        listsRepository
            .findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("List not found"));

    roleBoardService.checkRoleBoard(list.getBoard().getSlug(), userId, Const.CREATE_MODE);

    // 2. Load prev & next card
    Map<Integer, Card> refMap = getReferenceCards(request.afterId(), request.beforeId());

    // New: if client provided ids but they were not found, return 404 so client knows refs are
    // invalid
    if (request.afterId() != null && !refMap.containsKey(request.afterId())) {
      throw new ResourceNotFoundException("Prev card not found");
    }
    if (request.beforeId() != null && !refMap.containsKey(request.beforeId())) {
      throw new ResourceNotFoundException("Next card not found");
    }

    Card prev = request.afterId() != null ? refMap.get(request.afterId()) : null;
    Card next = request.beforeId() != null ? refMap.get(request.beforeId()) : null;

    // New: disallow referencing archived cards
    if (prev != null && Boolean.TRUE.equals(prev.getIsArchived())) {
      throw new InvalidInputException("Prev card is archived");
    }
    if (next != null && Boolean.TRUE.equals(next.getIsArchived())) {
      throw new InvalidInputException("Next card is archived");
    }

    // 3. Validate
    validateSameList(list, prev, next);
    validateOrder(prev, next);

    User user = userRepository.getReferenceById(userId);

    // 4. Append case
    if (prev == null && next == null) {
      return createAtEnd(list, request, user);
    }

    // 5. Resolve position
    var result = PositionUtils.resolve(prev, next, Card::getPosition);

    // 6. Rebalance nếu cần
    if (result.needRebalance()) {
      result = handleRebalanceCard(list, request.afterId(), request.beforeId());
    }

    // 7. Create card
    Card card =
        Card.builder()
            .title(request.title())
            .description(request.description())
            .list(list)
            .position(result.position())
            .createdBy(user)
            .build();

    CardMember member = new CardMember();
    member.setAssignedAt(LocalDateTime.now());

    user.addCardAssignment(member);
    card.addMember(member);

    Card saved = cardRepository.save(card);

    activityService.logCreateCard(saved, user);

    return cardMapper.toCardResponse(saved);
  }

  private Map<Integer, Card> getReferenceCards(Integer afterId, Integer beforeId) {

    Set<Integer> ids =
        Stream.of(afterId, beforeId).filter(Objects::nonNull).collect(Collectors.toSet());

    if (ids.isEmpty()) return Map.of();

    return cardRepository.findByIdIn(ids).stream().collect(Collectors.toMap(Card::getId, c -> c));
  }

  private void validateSameList(ListEntity list, Card prev, Card next) {

    if (prev != null && !prev.getList().getId().equals(list.getId())) {
      throw new InvalidInputException("Prev card not in same list");
    }

    if (next != null && !next.getList().getId().equals(list.getId())) {
      throw new InvalidInputException("Next card not in same list");
    }
  }

  private void validateOrder(Card prev, Card next) {
    if (prev != null && next != null && prev.getPosition().compareTo(next.getPosition()) >= 0) {
      throw new InvalidInputException("Invalid card order");
    }
  }

  private CardResponse createAtEnd(ListEntity list, CardRequest request, User user) {

    var maxPosition =
        cardRepository.findMaxPositionByListId(list.getId()).orElse(java.math.BigDecimal.ZERO);

    Card card =
        Card.builder()
            .title(request.title())
            .description(request.description())
            .list(list)
            .position(maxPosition.add(java.math.BigDecimal.valueOf(1000)))
            .createdBy(user)
            .build();
    Card cardRes = cardRepository.save(card);

    activityService.logCreateCard(cardRes, user);
    return cardMapper.toCardResponse(cardRes);
  }

  private PositionUtils.MoveResult<Card> handleRebalanceCard(
      ListEntity list, Integer afterId, Integer beforeId) {

    List<Card> cards =
        cardRepository.findByListIdAndIsArchivedFalseOrderByPositionAsc(list.getId());

    RebalanceUtils.rebalance(cards, Card::setPosition);

    cardRepository.saveAll(cards);

    Map<Integer, Card> map =
        cards.stream().collect(Collectors.toMap(Card::getId, Function.identity()));

    Card prev = (afterId != null) ? map.get(afterId) : null;
    Card next = (beforeId != null) ? map.get(beforeId) : null;

    return PositionUtils.resolve(prev, next, Card::getPosition);
  }

  @Transactional
  public void archiveCard(Integer cardId) {

    Card card = getCard(cardId);

    Integer userId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(card.getList().getBoard().getSlug(), userId, Const.DELETE_MODE);

    card.setIsArchived(true);

    User user = userRepository.getReferenceById(userId);
    activityService.logDeleteCard(card, user);
  }

  @Transactional
  public void updateCard(Integer cardId, CardUpdateRequest request) {

    Card card = getCard(cardId);

    // check quyền qua board
    roleBoardService.checkRoleBoard(
        card.getList().getBoard().getSlug(), authService.getCurrentUserId(), Const.UPDATE_MODE);

    boolean updated = false;

    if (request.title() != null) {
      card.setTitle(request.title());
      updated = true;
    }

    if (request.description() != null) {
      card.setDescription(request.description());
      updated = true;
    }

    if (request.dueDate() != null) {
      card.setDueDate(request.dueDate());
      updated = true;
    }

    if (request.isCompleted() != null) {
      card.setIsCompleted(request.isCompleted());

      if (Boolean.TRUE.equals(request.isCompleted())) {
        card.setCompletedAt(LocalDateTime.now());
      } else {
        card.setCompletedAt(null);
      }

      updated = true;
    }

    if (request.coverColor() != null) {
      card.setCoverColor(request.coverColor());
      updated = true;
    }

    if (request.coverImageUrl() != null) {
      card.setCoverImageUrl(request.coverImageUrl());
      updated = true;
    }

    if (request.dueReminder() != null) {
      card.setDueReminder(request.dueReminder());
      updated = true;
    }

    if (!updated) {
      throw new InvalidInputException("No fields to update");
    }
  }

  @Transactional
  public void updateCardPosition(Integer cardId, CardPositionRequest request) {

    Integer userId = authService.getCurrentUserId();

    // 1. Get current card
    Card card = getCard(cardId);

    // 2. Get target list
    ListEntity targetList =
        listsRepository
            .findById(request.listId())
            .orElseThrow(() -> new ResourceNotFoundException("List not found"));

    // 3. Permission (qua board)
    roleBoardService.checkRoleBoard(targetList.getBoard().getSlug(), userId, Const.UPDATE_MODE);

    // 4. Load prev & next
    Map<Integer, Card> refMap = getReferenceCards(request.afterId(), request.beforeId());

    Card prev = refMap.get(request.afterId());
    Card next = refMap.get(request.beforeId());

    // New: if client provided ids but they were not found, return 404 so client knows refs are
    // invalid
    if (request.afterId() != null && !refMap.containsKey(request.afterId())) {
      throw new ResourceNotFoundException("Prev card not found");
    }
    if (request.beforeId() != null && !refMap.containsKey(request.beforeId())) {
      throw new ResourceNotFoundException("Next card not found");
    }

    // New: disallow referencing archived cards
    if (prev != null && Boolean.TRUE.equals(prev.getIsArchived())) {
      throw new InvalidInputException("Prev card is archived");
    }
    if (next != null && Boolean.TRUE.equals(next.getIsArchived())) {
      throw new InvalidInputException("Next card is archived");
    }

    // Not move relative với chính nó
    if ((prev != null && prev.getId().equals(cardId))
        || (next != null && next.getId().equals(cardId))) {
      throw new InvalidInputException("Cannot move relative to itself");
    }

    // 5. Validate same list (target list)
    validateSameList(targetList, prev, next);
    validateOrder(prev, next);

    // 6. Move sang list mới nếu cần
    card.setList(targetList);

    // 7. Append case
    if (prev == null && next == null) {

      BigDecimal maxPos =
          cardRepository.findMaxPositionByListId(targetList.getId()).orElse(BigDecimal.ZERO);

      BigDecimal newPos = maxPos.add(new BigDecimal("1000"));
      card.setPosition(newPos);
      return;
    }

    // 8. Resolve position
    var result = PositionUtils.resolve(prev, next, Card::getPosition);

    // 9. Rebalance nếu cần
    if (result.needRebalance()) {
      result = handleRebalanceCard(targetList, request.afterId(), request.beforeId());
    }

    // 10. Set position
    card.setPosition(result.position());
  }

  private Card getCard(Integer cardId) {
    return cardRepository
        .findById(cardId)
        .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
  }

  @Transactional
  public CardDetailResponse getCardDetail(Integer cardId) {

    Card card = cardRepository.findById(cardId).orElseThrow();

    List<Label> boardLabels = labelRepository.findByBoardId(card.getList().getBoard().getId());
    List<CardLabel> cardLabels = cardLabelRepository.findByCardId(cardId);

    List<Checklist> checklists = checklistRepository.findByCardIdOrderByPosition(cardId);
    List<ChecklistItem> items = checklistItemRepository.findByChecklist_Card_Id(cardId);

    List<Attachment> attachments = attachmentRepository.findByCardId(cardId);

    return buildResponse(card, boardLabels, cardLabels, checklists, items, attachments);
  }

  private CardDetailResponse buildResponse(
      Card card,
      List<Label> boardLabels,
      List<CardLabel> cardLabels,
      List<Checklist> checklists,
      List<ChecklistItem> items,
      List<Attachment> attachments) {
    LabelGroupResponse labels = labelGroupMapper.map(boardLabels, cardLabels);
    List<ChecklistResponse> checklistRes = checklistMapper.map(checklists, items);
    List<AttachmentResponse> attachmentRes = attachmentMapper.toList(attachments);

    return new CardDetailResponse(
        card.getId(),
        card.getTitle(),
        card.getDescription(),
        card.getIsCompleted(),
        card.getDueDate(),
        card.getDueReminder(),
        labels,
        checklistRes,
        attachmentRes);
  }

  @Transactional
  public void moveCardToList(Integer cardId, Integer listId) {
    Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    ListEntity list = listsRepository.findById(listId).orElseThrow(() -> new ResourceNotFoundException("List not found"));

    Integer userId = authService.getCurrentUserId();
    roleBoardService.checkRoleBoard(list.getBoard().getSlug(), userId, Const.UPDATE_MODE);
    String fromList = card.getList().getName();
    String toList = list.getName();
    
    card.setList(list);
    Card saved = cardRepository.save(card);

    activityService.logMoveCard(saved, userRepository.getReferenceById(userId), fromList, toList);
  }
}
