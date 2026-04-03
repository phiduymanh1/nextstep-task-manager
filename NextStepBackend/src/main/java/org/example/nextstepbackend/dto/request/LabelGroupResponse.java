package org.example.nextstepbackend.dto.request;

import java.util.List;
import java.util.Set;

public record LabelGroupResponse(List<LabelResponse> boardLabels, Set<Integer> selectedLabelIds) {}
