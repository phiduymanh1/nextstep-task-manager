package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

  Page<Comment> findByCardId(Integer cardId, Pageable pageable);
}
