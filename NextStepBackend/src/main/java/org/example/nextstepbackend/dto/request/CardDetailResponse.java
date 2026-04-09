package org.example.nextstepbackend.dto.request;

import org.example.nextstepbackend.dto.response.user.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CardDetailResponse(
    Integer id,
    String title,
    String description,
    Boolean isCompleted,
    LocalDateTime dueDate,
    Boolean dueReminder,
    LabelGroupResponse labels,
    List<ChecklistResponse> checklists,
    List<AttachmentResponse> attachments,
    List<CardMemberResponse> members) {}
