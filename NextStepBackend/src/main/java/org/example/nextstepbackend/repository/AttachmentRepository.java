package org.example.nextstepbackend.repository;

import java.util.List;
import org.example.nextstepbackend.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

  List<Attachment> findByCardId(Integer cardId);
}
