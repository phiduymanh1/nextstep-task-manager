package org.example.nextstepbackend.services.list;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.ListPositionRequest;
import org.example.nextstepbackend.dto.request.ListsRequest;
import org.example.nextstepbackend.dto.request.ListsUpdateRequest;
import org.example.nextstepbackend.dto.response.card.CardResponse;
import org.example.nextstepbackend.dto.response.lists.ListDetailResponse;
import org.example.nextstepbackend.dto.response.lists.ListsResponse;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.ListEntity;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.CardMapper;
import org.example.nextstepbackend.mappers.ListMapper;
import org.example.nextstepbackend.repository.BoardRepository;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.ListsRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.board.RoleBoardService;
import org.example.nextstepbackend.utils.PositionUtils;
import org.example.nextstepbackend.utils.RebalanceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListService {

  private final ListsRepository listsRepository;
  private final BoardRepository boardRepository;
  private final ListMapper listMapper;
  private final AuthService authService;
  private final RoleBoardService roleBoardService;

  private final CardRepository cardRepository;
  private final CardMapper cardMapper;

  /** Create list with position resolved by afterId and beforeId */
  @Transactional
  public ListsResponse createListByBoardSlug(String boardSlug, ListsRequest request) {

    Integer userId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(boardSlug, userId, null, Const.CREATE_MODE);

    Board board = getBoard(boardSlug);

    // 1. Load prev & next (1 query)
    Map<Integer, ListEntity> refMap = getReferenceLists(request.afterId(), request.beforeId());

    ListEntity prev = refMap.get(request.afterId());
    ListEntity next = refMap.get(request.beforeId());

    // 2. Validate
    validateSameBoard(board, prev, next);
    validateOrder(prev, next);

    // 3. Append case (fast path)
    if (prev == null && next == null) {
      return createAtEnd(board, request);
    }

    // 4. Resolve position
    var result = PositionUtils.resolve(prev, next, ListEntity::getPosition);

    // 5. Rebalance if needed
    if (result.needRebalance()) {
      result = handleRebalance(board, request.afterId(), request.beforeId());
    }

    // 6. Create new list
    ListEntity entity = listMapper.toEntity(request);
    entity.setBoard(board);
    entity.setPosition(result.position());

    ListEntity list = listsRepository.save(entity);
    return new ListsResponse(list.getId(), list.getName(), list.getIsArchived());
  }

  /** Get board by slug or throw 404 */
  private Board getBoard(String slug) {
    return boardRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Board with slug " + slug + " not found"));
  }

  /** Load reference lists by afterId and beforeId (if provided) */
  private Map<Integer, ListEntity> getReferenceLists(Integer afterId, Integer beforeId) {

    List<Integer> ids = Stream.of(afterId, beforeId).filter(Objects::nonNull).distinct().toList();

    if (ids.isEmpty()) return Collections.emptyMap();

    Map<Integer, ListEntity> map =
        listsRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(ListEntity::getId, Function.identity()));

    // If caller provided ids but some were not found in DB, surface a 404 rather than silently
    // treating as null.
    Set<Integer> missing =
        ids.stream().filter(id -> !map.containsKey(id)).collect(Collectors.toSet());
    if (!missing.isEmpty()) {
      throw new ResourceNotFoundException("List(s) with id(s) " + missing + " not found");
    }

    return map;
  }

  private void validateSameBoard(Board board, ListEntity prev, ListEntity next) {

    if (prev != null && !prev.getBoard().getId().equals(board.getId())) {
      throw new InvalidInputException("afterId does not belong to this board");
    }

    if (next != null && !next.getBoard().getId().equals(board.getId())) {
      throw new InvalidInputException("beforeId does not belong to this board");
    }
  }

  private void validateOrder(ListEntity prev, ListEntity next) {
    if (prev != null && next != null && prev.getPosition().compareTo(next.getPosition()) >= 0) {

      throw new InvalidInputException("Invalid position: afterId must be before beforeId");
    }
  }

  /** Create list at the end of the board (when no afterId and beforeId) */
  private ListsResponse createAtEnd(Board board, ListsRequest request) {

    BigDecimal maxPos = listsRepository.findMaxPositionByBoardId(board.getId());
    BigDecimal newPos = (maxPos == null) ? BigDecimal.ONE : maxPos.add(new BigDecimal("1000"));

    ListEntity entity = listMapper.toEntity(request);
    entity.setBoard(board);
    entity.setPosition(newPos);

    ListEntity list = listsRepository.save(entity);

    return new ListsResponse(list.getId(), list.getName(), list.getIsArchived());
  }

  /** Rebalanced positions of all lists in the board and resolve position for new list */
  private PositionUtils.MoveResult<ListEntity> handleRebalance(
      Board board, Integer afterId, Integer beforeId) {

    List<ListEntity> lists = listsRepository.findByBoardIdOrderByPositionAsc(board.getId());

    // Rebalanced với step lớn
    RebalanceUtils.rebalance(lists, ListEntity::setPosition);

    listsRepository.saveAll(lists);

    // Map lại để không query DB nữa
    Map<Integer, ListEntity> map =
        lists.stream().collect(Collectors.toMap(ListEntity::getId, Function.identity()));

    ListEntity prev = (afterId != null) ? map.get(afterId) : null;

    ListEntity next = (beforeId != null) ? map.get(beforeId) : null;

    return PositionUtils.resolve(prev, next, ListEntity::getPosition);
  }

  /** Archive list by id (soft delete( */
  @Transactional
  public void archiveList(String slug, Integer listId) {

    // 1. Get list
    ListEntity list = findByBoardSlugAndId(slug, listId);

    Integer userId = authService.getCurrentUserId();

    roleBoardService.checkRoleBoard(
        slug, userId, list.getBoard().getWorkspace().getId(), Const.DELETE_MODE);

    // 2. Delete (soft delete nếu bạn có field archived)
    listsRepository.delete(list);
  }

  @Transactional
  public void updateList(String slug, Integer listId, ListsUpdateRequest request) {
    ListEntity list = findByBoardSlugAndId(slug, listId);

    roleBoardService.checkRoleBoard(
        slug,
        authService.getCurrentUserId(),
        list.getBoard().getWorkspace().getId(),
        Const.UPDATE_MODE);

    boolean updated = false;

    if (request.name() != null) {
      list.setName(request.name());
      updated = true;
    }

    if (!updated) {
      throw new InvalidInputException("No fields to update");
    }
  }

  @Transactional
  public void updateListPosition(String slug, Integer listId, ListPositionRequest request) {

    Integer userId = authService.getCurrentUserId();

    // 1. Get current list
    ListEntity list = findByBoardSlugAndId(slug, listId);

    // 2. Permission
    roleBoardService.checkRoleBoard(
        slug, userId, list.getBoard().getWorkspace().getId(), Const.UPDATE_MODE);

    Board board = list.getBoard();

    Map<Integer, ListEntity> refMap = getReferenceLists(request.afterId(), request.beforeId());

    ListEntity prev = refMap.get(request.afterId());
    ListEntity next = refMap.get(request.beforeId());

    if ((prev != null && prev.getId().equals(listId))
        || (next != null && next.getId().equals(listId))) {
      throw new InvalidInputException("Cannot move relative to itself");
    }

    validateSameBoard(board, prev, next);
    validateOrder(prev, next);

    // If both references missing -> append to end. Handle null maxPos and use consistent step.
    if (prev == null && next == null) {
      BigDecimal maxPos = listsRepository.findMaxPositionByBoardId(board.getId());
      BigDecimal newPos = (maxPos == null) ? BigDecimal.ONE : maxPos.add(new BigDecimal("1000"));
      list.setPosition(newPos);
      return;
    }

    var result = PositionUtils.resolve(prev, next, ListEntity::getPosition);

    if (result.needRebalance()) {
      result = handleRebalance(board, request.afterId(), request.beforeId());
    }

    list.setPosition(result.position());
  }

  private ListEntity findByBoardSlugAndId(String slug, Integer listId) {
    return listsRepository
        .findByBoard_SlugAndId(slug, listId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "List with id " + listId + " not found in board " + slug));
  }

  public ListDetailResponse getListDetail(Integer listId) {

    String email = authService.getCurrentUser().getEmail();

    // check quyền + lấy list
    ListEntity list =
        listsRepository
            .findByIdAndMember(listId, email)
            .orElseThrow(() -> new ResourceNotFoundException("List not found"));

    List<Card> cards = cardRepository.findByListIdAndIsArchivedFalseOrderByPositionAsc(listId);

    List<CardResponse> cardResponses = cards.stream().map(cardMapper::toCardResponse).toList();

    return new ListDetailResponse(list.getId(), list.getName(), list.getPosition(), cardResponses);
  }
}
