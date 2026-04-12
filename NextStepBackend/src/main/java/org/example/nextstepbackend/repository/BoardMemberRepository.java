package org.example.nextstepbackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.nextstepbackend.entity.Board;
import org.example.nextstepbackend.entity.BoardMember;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.enums.BoardRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer> {

  Optional<BoardMember> findByBoard_SlugAndUser_Id(String boardSlug, Integer id);

  List<BoardMember> findByBoard_Slug(String slug);

  boolean existsByBoardAndUser(Board board, User user);

  Optional<BoardMember> findByBoardAndUser(Board board, User user);

  long countByBoardAndRole(Board board, BoardRole role);
}
