package org.example.nextstepbackend.mappers;

import java.util.List;
import org.example.nextstepbackend.dto.request.ActivityResponse;
import org.example.nextstepbackend.entity.Activity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

  ActivityResponse toResponse(Activity activity);

  List<ActivityResponse> toList(List<Activity> activities);
}
