package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.AttachmentResponse;
import org.example.nextstepbackend.entity.Attachment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

  AttachmentResponse toResponse(Attachment attachment);

  List<AttachmentResponse> toList(List<Attachment> attachments);
}
