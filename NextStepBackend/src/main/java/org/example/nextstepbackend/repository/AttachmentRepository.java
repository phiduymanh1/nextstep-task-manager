package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

  List<Attachment> findByCardId(Integer cardId);
}
