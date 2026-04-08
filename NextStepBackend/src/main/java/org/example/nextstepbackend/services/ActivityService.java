package org.example.nextstepbackend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.ActivityResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.entity.Activity;
import org.example.nextstepbackend.entity.Attachment;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.Checklist;
import org.example.nextstepbackend.entity.ChecklistItem;
import org.example.nextstepbackend.entity.Label;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.enums.ActionType;
import org.example.nextstepbackend.enums.EntityType;
import org.example.nextstepbackend.exceptions.AppException;
import org.example.nextstepbackend.mappers.ActivityMapper;
import org.example.nextstepbackend.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

  private final ActivityRepository activityRepository;
  private final ActivityMapper activityMapper;
  private final ObjectMapper objectMapper;

  private static final String CART_TITLE = "cardTitle";

  public PageResponse<ActivityResponse> getActivities(Integer cardId, Pageable pageable) {
    Page<ActivityResponse> page =
        activityRepository.findByCardId(cardId, pageable).map(activityMapper::toResponse);

    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public void create(Activity activity) {

    if (!isImportant(activity.getActionType())) {
      return;
    }

    activityRepository.save(activity);
  }

  private String toJson(Map<String, Object> metadata) {
    try {
      return objectMapper.writeValueAsString(metadata);
    } catch (JsonProcessingException e) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Convert metadata to JSON failed", "CONVERT_FAIL");
    }
  }

  @SuppressWarnings("java:S107")
  private Activity buildActivity(
      Card card,
      Board board,
      User user,
      ActionType actionType,
      EntityType entityType,
      Integer entityId,
      String message,
      Map<String, Object> metadata) {
    return Activity.builder()
        .card(card)
        .board(board)
        .user(user)
        .actionType(actionType)
        .entityType(entityType)
        .entityId(entityId)
        .message(message)
        .metadata(toJson(metadata))
        .build();
  }

  // 1. Move card
  public void logMoveCard(Card card, User user, String fromList, String toList) {

    String message = user.getFullName() + " đã chuyển card";

    Map<String, Object> metadata =
        Map.of(
            "fromList", fromList,
            "toList", toList);

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.MOVE_CARD,
            EntityType.CARD,
            card.getId(),
            message,
            metadata);

    create(activity);
  }

  // 2. Create card
  public void logCreateCard(Card card, User user) {

    String message = user.getFullName() + " đã tạo card " + card.getTitle();

    Map<String, Object> metadata = Map.of(CART_TITLE, card.getTitle());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.CREATE_CARD,
            EntityType.CARD,
            card.getId(),
            message,
            metadata);

    create(activity);
  }

  // 3. Delete card
  public void logDeleteCard(Card card, User user) {

    String message = user.getFullName() + " đã xóa card " + card.getTitle();

    Map<String, Object> metadata = Map.of(CART_TITLE, card.getTitle());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.DELETE_CARD,
            EntityType.CARD,
            card.getId(),
            message,
            metadata);

    create(activity);
  }

  // 6. Create check list
  public void logCreateChecklist(Card card, User user, Checklist checklist) {

    String message = user.getFullName() + " đã thêm checklist " + checklist.getTitle();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE,
            card.getTitle(),
            "checklistId",
            checklist.getId(),
            "checklistTile",
            checklist.getTitle());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.CREATE_CHECKLIST,
            EntityType.CHECKLIST,
            checklist.getId(),
            message,
            metadata);

    create(activity);
  }

  // 7, add check list item
  public void logAddChecklistItem(Card card, User user, ChecklistItem item) {

    String message = user.getFullName() + " đã thêm mục checklist " + item.getContent();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE, card.getTitle(), "itemId", item.getId(), "itemContent", item.getContent());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.ADD_CHECKLIST_ITEM,
            EntityType.CHECKLIST_ITEM,
            item.getId(),
            message,
            metadata);

    create(activity);
  }

  // 8. log complete checklist
  public void logCompleteChecklistItem(Card card, User user, ChecklistItem item) {

    String message = user.getFullName() + " đã hoàn thành mục checklist " + item.getContent();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE,
            card.getTitle(),
            "itemId",
            item.getId(),
            "itemContent",
            item.getContent(),
            "isCompleted",
            item.getIsCompleted());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.COMPLETE_CHECKLIST_ITEM,
            EntityType.CHECKLIST_ITEM,
            item.getId(),
            message,
            metadata);

    create(activity);
  }

  // 9. delete checklist TODO:
  public void logDeleteChecklist(Card card, User user, Checklist checklist) {

    String message = user.getFullName() + " đã xóa checklist " + checklist.getTitle();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE,
            card.getTitle(),
            "checklistId",
            checklist.getId(),
            "checklistTitle",
            checklist.getTitle());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.DELETE_CHECKLIST,
            EntityType.CHECKLIST,
            checklist.getId(),
            message,
            metadata);

    create(activity);
  }

  // 10. add attachment
  public void logAddAttachment(Card card, User user, Attachment attachment) {

    String message = user.getFullName() + " đã thêm file đính kèm " + attachment.getFileName();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE,
            card.getTitle(),
            "attachmentId",
            attachment.getId(),
            "fileName",
            attachment.getFileName(),
            "url",
            attachment.getFileUrl());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.ADD_ATTACHMENT,
            EntityType.ATTACHMENT,
            attachment.getId(),
            message,
            metadata);

    create(activity);
  }

  // 11. delete attachment
  public void logDeleteAttachment(Card card, User user, Attachment attachment) {

    String message = user.getFullName() + " đã xóa file đính kèm " + attachment.getFileName();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE,
            card.getTitle(),
            "attachmentId",
            attachment.getId(),
            "fileName",
            attachment.getFileName());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.DELETE_ATTACHMENT,
            EntityType.ATTACHMENT,
            attachment.getId(),
            message,
            metadata);

    create(activity);
  }

  // 12. add label to card
  public void logAddLabelToCard(Card card, User user, Label label) {

    String message = user.getFullName() + " đã thêm label " + label.getName();

    Map<String, Object> metadata =
        Map.of(
            CART_TITLE,
            card.getTitle(),
            "labelId",
            label.getId(),
            "labelName",
            label.getName(),
            "color",
            label.getColor());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.ADD_LABEL_TO_CARD,
            EntityType.LABEL,
            label.getId(),
            message,
            metadata);

    create(activity);
  }

  // 13. remove label from card

  public void logRemoveLabelFromCard(Card card, User user, Label label) {

    String message = user.getFullName() + " đã xóa label " + label.getName();

    Map<String, Object> metadata =
        Map.of(CART_TITLE, card.getTitle(), "labelId", label.getId(), "labelName", label.getName());

    Activity activity =
        buildActivity(
            card,
            card.getList().getBoard(),
            user,
            ActionType.REMOVE_LABEL_FROM_CARD,
            EntityType.LABEL,
            label.getId(),
            message,
            metadata);

    create(activity);
  }

  private boolean isImportant(ActionType actionType) {
    return switch (actionType) {
      case CREATE_CARD,
              UPDATE_CARD,
              DELETE_CARD,
              MOVE_CARD,
              ADD_LABEL,
              CREATE_CHECKLIST,
              ADD_CHECKLIST_ITEM,
              COMPLETE_CHECKLIST_ITEM,
              DELETE_CHECKLIST,
              ADD_ATTACHMENT,
              DELETE_ATTACHMENT,
              ADD_LABEL_TO_CARD,
              REMOVE_LABEL_FROM_CARD ->
          true;
      default -> false;
    };
  }
}
