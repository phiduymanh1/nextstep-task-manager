package org.example.nextstepbackend.services.label;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.dto.request.BoardLabelRequest;
import org.example.nextstepbackend.dto.request.LabelResponse;
import org.example.nextstepbackend.dto.request.SelectedCardLabelRequest;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.CardLabel;
import org.example.nextstepbackend.entity.Label;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.LabelMapper;
import org.example.nextstepbackend.repository.BoardRepository;
import org.example.nextstepbackend.repository.CardLabelRepository;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.repository.LabelRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.board.RoleBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabelService {

  private final BoardRepository boardRepository;
  private final LabelMapper labelMapper;
  private final LabelRepository labelRepository;
  private final RoleBoardService roleBoardService;
  private final AuthService authService;
  private final CardRepository cardRepository;
  private final CardLabelRepository cardLabelRepository;

  @Transactional
  public LabelResponse createBoardLabel(String boardSlug, BoardLabelRequest request) {

    Board board =
        boardRepository
            .findBySlug(boardSlug)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        String.format("Board with slug '%s' not found", boardSlug)));

    roleBoardService.checkRoleBoard(
        boardSlug, authService.getCurrentUserId(), board.getWorkspace().getId(), Const.CREATE_MODE);

    Label label = labelMapper.toEntity(request);
    label.setBoard(board);

    Label res = labelRepository.save(label);

    return labelMapper.toResponse(res);
  }

  @Transactional
  public void selectedCardLabel(SelectedCardLabelRequest request) {
    Card card =
        cardRepository
            .findById(request.cardId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Card not found with id: " + request.cardId()));

    Label label =
        labelRepository
            .findById(request.labelId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Label not found with id: " + request.labelId()));

    CardLabel cardLabel = new CardLabel();
    cardLabel.setCard(card);
    cardLabel.setLabel(label);

    cardLabelRepository.save(cardLabel);
  }
}
