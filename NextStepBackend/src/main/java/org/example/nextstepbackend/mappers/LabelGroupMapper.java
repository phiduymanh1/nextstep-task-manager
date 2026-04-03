package org.example.nextstepbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.LabelGroupResponse;
import org.example.nextstepbackend.entity.CardLabel;
import org.example.nextstepbackend.entity.Label;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LabelGroupMapper {

  private final LabelMapper labelMapper;

  public LabelGroupResponse map(List<Label> boardLabels, List<CardLabel> cardLabels) {

    Set<Integer> selectedIds =
        cardLabels.stream().map(cl -> cl.getLabel().getId()).collect(Collectors.toSet());

    return new LabelGroupResponse(labelMapper.toList(boardLabels), selectedIds);
  }
}
