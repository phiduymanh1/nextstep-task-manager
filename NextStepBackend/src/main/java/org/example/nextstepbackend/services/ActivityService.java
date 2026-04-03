package org.example.nextstepbackend.services;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.ActivityResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.mappers.ActivityMapper;
import org.example.nextstepbackend.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

  private final ActivityRepository activityRepository;
  private final ActivityMapper activityMapper;

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
}
