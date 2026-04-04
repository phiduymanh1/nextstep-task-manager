package org.example.nextstepbackend.services.comment;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.CommentRequest;
import org.example.nextstepbackend.dto.request.CommentResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.Comment;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.CommentMapper;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.CommentRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;
  private final CardRepository cardRepository;
  private final UserRepository userRepository;
  private final AuthService authService;

  @Transactional
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

  @Transactional
  public CommentResponse commentCard(Integer cardId,CommentRequest request){

    Card card = cardRepository.findById(cardId).orElseThrow(
            () -> new ResourceNotFoundException("Card not found with id: " + cardId)
    );

    User user = userRepository.findById(authService.getCurrentUserId()).orElseThrow(
            () -> new ResourceNotFoundException("User not found with id: " + authService.getCurrentUserId())
    );

    Comment comment = new Comment();
    comment.setCard(card);
    comment.setUser(user);
    comment.setContent(request.content());

    Comment res = commentRepository.save(comment);

    return commentMapper.toResponse(res);
  }
}
