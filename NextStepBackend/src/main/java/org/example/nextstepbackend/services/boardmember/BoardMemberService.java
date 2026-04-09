package org.example.nextstepbackend.services.boardmember;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.BoardMemberResponse;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.repository.BoardMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardMemberService {

    private final BoardMemberRepository boardMemberRepository;

    @Transactional
    public List<BoardMemberResponse> getBoardMembers(String boardSlug) {

        List<BoardMember> members = boardMemberRepository.findByBoard_Slug(boardSlug);

        return members.stream()
                .map(m -> new BoardMemberResponse(
                        m.getUser().getId(),
                        m.getUser().getFullName(),
                        m.getUser().getAvatarUrl(),
                        m.getRole()
                ))
                .toList();
    }
}
