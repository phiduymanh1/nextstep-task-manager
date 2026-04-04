package org.example.nextstepbackend.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.nextstepbackend.dto.request.ChecklistItemResponse;
import org.example.nextstepbackend.dto.request.ChecklistResponse;
import org.example.nextstepbackend.entity.Checklist;
import org.example.nextstepbackend.entity.ChecklistItem;
import org.springframework.stereotype.Component;

@Component
public class ChecklistMapper {

  public List<ChecklistResponse> map(List<Checklist> checklists, List<ChecklistItem> items) {

    Map<Integer, List<ChecklistItem>> itemMap =
        items.stream().collect(Collectors.groupingBy(i -> i.getChecklist().getId()));

    return checklists.stream()
        .map(
            cl ->
                new ChecklistResponse(
                    cl.getId(),
                    cl.getTitle(),
                    cl.getPosition(),
                    itemMap.getOrDefault(cl.getId(), List.of()).stream()
                        .map(
                            i ->
                                new ChecklistItemResponse(
                                    i.getId(),
                                    i.getContent(),
                                    i.getIsCompleted(),
                                    i.getCompletedBy() != null ? i.getCompletedBy().getId() : null,
                                    i.getCompletedAt(),
                                    i.getPosition(),
                                    i.getDueDate()))
                        .toList()))
        .toList();
  }
}
