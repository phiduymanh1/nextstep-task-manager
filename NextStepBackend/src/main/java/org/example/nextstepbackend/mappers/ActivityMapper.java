package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.ActivityResponse;
import org.example.nextstepbackend.entity.Activity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

  ActivityResponse toResponse(Activity activity);

  List<ActivityResponse> toList(List<Activity> activities);
}
