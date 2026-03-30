package org.example.nextstepbackend.services.list;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.ListsRequest;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.ListEntity;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.ListMapper;
import org.example.nextstepbackend.repository.BoardRepository;
import org.example.nextstepbackend.repository.ListsRepository;
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

  @Transactional
  public void createListByBoardSlug(String boardSlug, ListsRequest request) {

    Board board = getBoard(boardSlug);

    // 1. Load prev & next (1 query)
    Map<Integer, ListEntity> refMap = getReferenceLists(request);

    ListEntity prev = refMap.get(request.afterId());
    ListEntity next = refMap.get(request.beforeId());

    // 2. Validate
    validateSameBoard(board, prev, next);
    validateOrder(prev, next);

    // 3. Append case (fast path)
    if (prev == null && next == null) {
      createAtEnd(board, request);
      return;
    }

    // 4. Resolve position
    var result = PositionUtils.resolve(prev, next, ListEntity::getPosition);

    // 5. Rebalance if needed
    if (result.needRebalance()) {
      result = handleRebalance(board, request);
    }

    // 6. Create new list
    ListEntity entity = listMapper.toEntity(request);
    entity.setBoard(board);
    entity.setPosition(result.position());

    listsRepository.save(entity);
  }

  private Board getBoard(String slug) {
    return boardRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Board with slug " + slug + " not found"));
  }

  private Map<Integer, ListEntity> getReferenceLists(ListsRequest request) {

    List<Integer> ids =
        Stream.of(request.afterId(), request.beforeId())
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    if (ids.isEmpty()) return Collections.emptyMap();

    return listsRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(ListEntity::getId, Function.identity()));
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

  private void createAtEnd(Board board, ListsRequest request) {

    BigDecimal maxPos = listsRepository.findMaxPositionByBoardId(board.getId());
    BigDecimal newPos = (maxPos == null) ? BigDecimal.ONE : maxPos.add(new BigDecimal("1000"));

    ListEntity entity = listMapper.toEntity(request);
    entity.setBoard(board);
    entity.setPosition(newPos);

    listsRepository.save(entity);
  }

  private PositionUtils.MoveResult<ListEntity> handleRebalance(Board board, ListsRequest request) {

    List<ListEntity> lists = listsRepository.findByBoardIdOrderByPositionAsc(board.getId());

    // Rebalance với step lớn
    RebalanceUtils.rebalance(lists, ListEntity::setPosition);

    listsRepository.saveAll(lists);

    // Map lại để không query DB nữa
    Map<Integer, ListEntity> map =
        lists.stream().collect(Collectors.toMap(ListEntity::getId, Function.identity()));

    ListEntity prev = (request.afterId() != null) ? map.get(request.afterId()) : null;

    ListEntity next = (request.beforeId() != null) ? map.get(request.beforeId()) : null;

    return PositionUtils.resolve(prev, next, ListEntity::getPosition);
  }
}
