package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.CardMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardMemberRepository extends JpaRepository<CardMember, Integer> {

  void deleteByUser_IdAndCard_Id(Integer userId, Integer cardId);
}
