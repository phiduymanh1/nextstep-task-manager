package org.example.nextstepbackend.services.checklist;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.ChecklistRequest;
import org.example.nextstepbackend.dto.request.ChecklistResponse;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.Checklist;
import org.example.nextstepbackend.exceptions.InvalidInputException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.CheckListsMapper;
import org.example.nextstepbackend.mappers.ChecklistMapper;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.ChecklistRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.board.RoleBoardService;
import org.example.nextstepbackend.utils.PositionUtils;
import org.example.nextstepbackend.utils.RebalanceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final AuthService authService;
    private final CardRepository cardRepository;
    private final RoleBoardService roleBoardService;
    private final ChecklistRepository checklistRepository;
    private final ChecklistMapper checklistMapper;
    private final CheckListsMapper checkListsMapper;

    @Transactional
    public ChecklistResponse createChecklist(Integer cardId, ChecklistRequest request) {

        Integer userId = authService.getCurrentUserId();

        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ResourceNotFoundException("Card not found")
        );

        roleBoardService.checkRoleBoard(
                card.getList().getBoard().getSlug(),
                userId,
                null,
                Const.CREATE_MODE
        );

        // 2. Load prev & next checklist
        Map<Integer, Checklist> refMap =
                getReferenceChecklists(request.afterId(), request.beforeId());

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

        // 4. Append case
        if (prev == null && next == null) {
            return createChecklistAtEnd(card, request);
        }

        // 5. Resolve position
        var result = PositionUtils.resolve(prev, next, Checklist::getPosition);

        // 6. Rebalance nếu cần
        if (result.needRebalance()) {
            result = handleRebalanceChecklist(card, request.afterId(), request.beforeId());
        }

        // 7. Create checklist
        Checklist checklist = Checklist.builder()
                .title(request.title())
                .card(card)
                .position(result.position())
                .build();

        Checklist saved = checklistRepository.save(checklist);

        return checkListsMapper.toResponse(saved);
    }

    private Map<Integer, Checklist> getReferenceChecklists(Integer afterId, Integer beforeId) {

        Set<Integer> ids =
                Stream.of(afterId, beforeId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        if (ids.isEmpty()) return Map.of();

        return checklistRepository.findByIdIn(ids)
                .stream()
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
        if (prev != null && next != null &&
                prev.getPosition().compareTo(next.getPosition()) >= 0) {
            throw new InvalidInputException("Invalid checklist order");
        }
    }

    private ChecklistResponse createChecklistAtEnd(Card card, ChecklistRequest request) {

        var maxPosition =
                checklistRepository.findMaxPositionByCardId(card.getId())
                        .orElse(BigDecimal.ZERO);

        Checklist checklist = Checklist.builder()
                .title(request.title())
                .card(card)
                .position(maxPosition.add(BigDecimal.valueOf(1000)))
                .build();

        return checkListsMapper.toResponse(checklistRepository.save(checklist));
    }

    private PositionUtils.MoveResult<Checklist> handleRebalanceChecklist(
            Card card, Integer afterId, Integer beforeId) {

        List<Checklist> checklists =
                checklistRepository.findByCardIdOrderByPositionAsc(card.getId());

        RebalanceUtils.rebalance(checklists, Checklist::setPosition);

        checklistRepository.saveAll(checklists);

        Map<Integer, Checklist> map =
                checklists.stream()
                        .collect(Collectors.toMap(Checklist::getId, Function.identity()));

        Checklist prev = (afterId != null) ? map.get(afterId) : null;
        Checklist next = (beforeId != null) ? map.get(beforeId) : null;

        return PositionUtils.resolve(prev, next, Checklist::getPosition);
    }
}
