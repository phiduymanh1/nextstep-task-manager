package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer> {

  Optional<BoardMember> findByBoard_SlugAndUser_Id(String boardSlug, Integer id);
}
