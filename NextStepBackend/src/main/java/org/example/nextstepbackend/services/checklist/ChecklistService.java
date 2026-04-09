package org.example.nextstepbackend.services.checklist;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.ChecklistItemRequest;
import org.example.nextstepbackend.dto.request.ChecklistItemResponse;
import org.example.nextstepbackend.dto.request.ChecklistRequest;
import org.example.nextstepbackend.dto.request.ChecklistResponse;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.Checklist;
import org.example.nextstepbackend.entity.ChecklistItem;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.CheckListsMapper;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.ChecklistItemRepository;
import org.example.nextstepbackend.repository.ChecklistRepository;
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
public class ChecklistService {

  private final AuthService authService;
  private final CardRepository cardRepository;
  private final RoleBoardService roleBoardService;
  private final ChecklistRepository checklistRepository;
  private final CheckListsMapper checkListsMapper;
  private final ChecklistItemRepository checklistItemRepository;
  private final ActivityService activityService;
  private final UserRepository userRepository;

  @Transactional
  public ChecklistResponse createChecklist(Integer cardId, ChecklistRequest request) {

    Integer userId = authService.getCurrentUserId();

    Card card =
        cardRepository
            .findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

    roleBoardService.checkRoleBoard(card.getList().getBoard().getSlug(), userId, Const.CREATE_MODE);

    // 2. Load prev & next checklist
    Map<Integer, Checklist> refMap = getReferenceChecklists(request.afterId(), request.beforeId());

    if (request.afterId() != null && !refMap.containsKey(request.afterId())) {
      throw new ResourceNotFoundException("Prev checklist not found");
    }
    if (request.beforeId() != null && !refMap.containsKey(request.beforeId())) {
      throw new ResourceNotFoundException("Next checklist not found");
    }

    Checklist prev = request.afterId() != null ? refMap.get(request.afterId()) : null;
    Checklist next = request.beforeId() != null ? refMap.get(request.beforeId()) : null;

    // 3. Validate
    validateSameCard(card, prev, next);
    validateOrder(prev, next);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    // 4. Append case
    if (prev == null && next == null) {
      return createChecklistAtEnd(card, request, user);
    }

    // 5. Resolve position
    var result = PositionUtils.resolve(prev, next, Checklist::getPosition);

    // 6. Rebalance nếu cần
    if (result.needRebalance()) {
      result = handleRebalanceChecklist(card, request.afterId(), request.beforeId());
    }

    // 7. Create checklist
    Checklist checklist =
        Checklist.builder().title(request.title()).card(card).position(result.position()).build();

    Checklist saved = checklistRepository.save(checklist);

    activityService.logCreateChecklist(card, user, saved);

    return checkListsMapper.toResponse(saved);
  }

  private Map<Integer, Checklist> getReferenceChecklists(Integer afterId, Integer beforeId) {

    Set<Integer> ids =
        Stream.of(afterId, beforeId).filter(Objects::nonNull).collect(Collectors.toSet());

    if (ids.isEmpty()) return Map.of();

    return checklistRepository.findByIdIn(ids).stream()
        .collect(Collectors.toMap(Checklist::getId, c -> c));
  }

  private void validateSameCard(Card card, Checklist prev, Checklist next) {

    if (prev != null && !prev.getCard().getId().equals(card.getId())) {
      throw new InvalidInputException("Prev checklist not in same card");
    }

    if (next != null && !next.getCard().getId().equals(card.getId())) {
      throw new InvalidInputException("Next checklist not in same card");
    }
  }

  private void validateOrder(Checklist prev, Checklist next) {
    if (prev != null && next != null && prev.getPosition().compareTo(next.getPosition()) >= 0) {
      throw new InvalidInputException("Invalid checklist order");
    }
  }

  private ChecklistResponse createChecklistAtEnd(Card card, ChecklistRequest request, User user) {

    var maxPosition =
        checklistRepository.findMaxPositionByCardId(card.getId()).orElse(BigDecimal.ZERO);

    Checklist checklist =
        Checklist.builder()
            .title(request.title())
            .card(card)
            .position(maxPosition.add(BigDecimal.valueOf(1000)))
            .build();

    Checklist res = checklistRepository.save(checklist);
    activityService.logCreateChecklist(card, user, res);

    return checkListsMapper.toResponse(res);
  }

  private PositionUtils.MoveResult<Checklist> handleRebalanceChecklist(
      Card card, Integer afterId, Integer beforeId) {

    List<Checklist> checklists = checklistRepository.findByCardIdOrderByPositionAsc(card.getId());

    RebalanceUtils.rebalance(checklists, Checklist::setPosition);

    checklistRepository.saveAll(checklists);

    Map<Integer, Checklist> map =
        checklists.stream().collect(Collectors.toMap(Checklist::getId, Function.identity()));

    Checklist prev = (afterId != null) ? map.get(afterId) : null;
    Checklist next = (beforeId != null) ? map.get(beforeId) : null;

    return PositionUtils.resolve(prev, next, Checklist::getPosition);
  }

  @Transactional
  public ChecklistItemResponse createChecklistItem(
      Integer checklistId, ChecklistItemRequest request) {
    Checklist checklist = findChecklistOrThrow(checklistId);

    ChecklistItem prev = null;
    ChecklistItem next = null;

    Map<Integer, ChecklistItem> refMap =
        getReferenceItems(request.afterId(), request.beforeId(), checklist);

    if (request.afterId() != null) prev = refMap.get(request.afterId());
    if (request.beforeId() != null) next = refMap.get(request.beforeId());

    validateOrder(prev, next);

    BigDecimal position = resolvePosition(checklist, prev, next);

    ChecklistItem saved = saveChecklistItem(checklist, request, position);

    return mapToResponse(saved);
  }

  private void validateOrder(ChecklistItem prev, ChecklistItem next) {
    if (prev != null && next != null && prev.getPosition().compareTo(next.getPosition()) >= 0) {
      throw new InvalidInputException("Invalid checklist item order");
    }
  }

  private Checklist findChecklistOrThrow(Integer checklistId) {
    return checklistRepository
        .findById(checklistId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Checklist not found with id: " + checklistId));
  }

  private BigDecimal resolvePosition(Checklist checklist, ChecklistItem prev, ChecklistItem next) {

    if (prev == null && next == null) {
      return appendToEnd(checklist);
    }

    if (prev == null) {
      return insertBefore(next);
    }

    if (next == null) {
      return insertAfter(prev);
    }

    return insertBetween(checklist, prev, next);
  }

  private BigDecimal appendToEnd(Checklist checklist) {
    return findMaxItemPosition(checklist).add(BigDecimal.valueOf(1000));
  }

  private BigDecimal insertBefore(ChecklistItem next) {
    return next.getPosition().subtract(BigDecimal.valueOf(1000));
  }

  private BigDecimal insertAfter(ChecklistItem prev) {
    return prev.getPosition().add(BigDecimal.valueOf(1000));
  }

  private BigDecimal insertBetween(Checklist checklist, ChecklistItem prev, ChecklistItem next) {
    BigDecimal gap = next.getPosition().subtract(prev.getPosition());

    if (gap.compareTo(BigDecimal.ONE) <= 0) {
      rebalance(checklist);
      return prev.getPosition().add(BigDecimal.valueOf(500));
    }

    return prev.getPosition()
        .add(next.getPosition())
        .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
  }

  private void rebalance(Checklist checklist) {
    List<ChecklistItem> items =
        checklist.getItems().stream()
            .sorted(Comparator.comparing(ChecklistItem::getPosition))
            .toList();

    BigDecimal pos = BigDecimal.valueOf(1000);

    for (ChecklistItem item : items) {
      item.setPosition(pos);
      pos = pos.add(BigDecimal.valueOf(1000));
    }

    checklistItemRepository.saveAll(items);
  }

  private ChecklistItem saveChecklistItem(
      Checklist checklist, ChecklistItemRequest request, BigDecimal position) {

    ChecklistItem item =
        ChecklistItem.builder()
            .checklist(checklist)
            .content(request.content())
            .position(position)
            .isCompleted(false)
            .dueDate(request.dueDate())
            .build();

    return checklistItemRepository.save(item);
  }

  private ChecklistItemResponse mapToResponse(ChecklistItem saved) {
    return new ChecklistItemResponse(
        saved.getId(),
        saved.getContent(),
        saved.getIsCompleted(),
        saved.getCompletedBy() != null ? saved.getCompletedBy().getId() : null,
        saved.getCompletedAt(),
        saved.getPosition(),
        saved.getDueDate());
  }

  private Map<Integer, ChecklistItem> getReferenceItems(
      Integer afterId, Integer beforeId, Checklist checklist) {
    Set<Integer> ids =
        Stream.of(afterId, beforeId).filter(Objects::nonNull).collect(Collectors.toSet());

    if (ids.isEmpty()) return Map.of();

    return checklist.getItems().stream()
        .filter(item -> ids.contains(item.getId()))
        .collect(Collectors.toMap(ChecklistItem::getId, Function.identity()));
  }

  private BigDecimal findMaxItemPosition(Checklist checklist) {
    return checklist.getItems().stream()
        .map(ChecklistItem::getPosition)
        .max(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);
  }

  @Transactional
  public ChecklistItemResponse toggleChecklistItem(Integer itemId) {
    ChecklistItem item =
        checklistItemRepository
            .findById(itemId)
            .orElseThrow(() -> new RuntimeException("Checklist item not found"));

    roleBoardService.checkRoleBoard(
        item.getChecklist().getCard().getList().getBoard().getSlug(),
        authService.getCurrentUserId(),
        Const.UPDATE_MODE);

    item.setIsCompleted(!item.getIsCompleted());
    ChecklistItem saved = checklistItemRepository.save(item);
    User user = userRepository.getReferenceById(authService.getCurrentUserId());

    if (item.getIsCompleted()){
      activityService.logCompleteChecklistItem(saved.getChecklist().getCard(), user, saved);
    }else {
      activityService.deleteCompleteChecklistItemActivity(item.getId());
    }

    return mapToResponse(saved);
  }

  @Transactional
  public void deleteChecklistItem(Integer itemId) {
    ChecklistItem item =
        checklistItemRepository
            .findById(itemId)
            .orElseThrow(() -> new RuntimeException("Checklist item not found"));

    Card card = item.getChecklist().getCard();
    Integer userId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(
        card.getList().getBoard().getSlug(), userId, Const.DELETE_MODE);

    checklistItemRepository.delete(item);
  }

  @Transactional
  public void deleteChecklist(Integer checklistId) {
    Checklist checklist =
        checklistRepository
            .findById(checklistId)
            .orElseThrow(() -> new RuntimeException("Checklist not found"));

    Card card = checklist.getCard();
    Integer userId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(card.getList().getBoard().getSlug(), userId, Const.DELETE_MODE);

    checklistRepository.delete(checklist);

    activityService.logDeleteChecklist(card, userRepository.getReferenceById(userId), checklist);
  }
}
