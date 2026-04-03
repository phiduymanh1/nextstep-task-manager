package org.example.nextstepbackend.services.comment;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.CommentResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.mappers.CommentMapper;
import org.example.nextstepbackend.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  public PageResponse<CommentResponse> getComments(Integer cardId, Pageable pageable) {
    Page<CommentResponse> page =
        commentRepository.findByCardId(cardId, pageable).map(commentMapper::toResponse);

    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
