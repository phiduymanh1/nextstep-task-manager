package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.CardMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardMemberRepository extends JpaRepository<CardMember, Integer> {

  void deleteByUser_IdAndCard_Id(Integer userId, Integer cardId);

  List<CardMember> findByCardId(Integer cardId);
}
