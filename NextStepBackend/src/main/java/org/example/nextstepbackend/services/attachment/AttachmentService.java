package org.example.nextstepbackend.services.attachment;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.AttachmentResponse;
import org.example.nextstepbackend.dto.response.cloud.UploadResult;
import org.example.nextstepbackend.entity.Attachment;
import org.example.nextstepbackend.entity.Card;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.AppException;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.repository.AttachmentRepository;
import org.example.nextstepbackend.repository.CardRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.cloudinary.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentService {

  private final CardRepository cardRepository;
  private final AuthService authService;
  private final FileStorageService fileStorageService;
  private final AttachmentRepository attachmentRepository;
  private final CloudinaryService cloudinaryService;

  public AttachmentResponse upload(Integer cardId, MultipartFile file) {

    if (file.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "File is empty", "FILE-EMPTY");
    }

    Card card =
        cardRepository
            .findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

    User user = authService.getCurrentUser();

    String publicId = "card_" + cardId + "_" + System.currentTimeMillis();

    UploadResult result = cloudinaryService.uploadAttachment(file, publicId);

    Attachment attachment =
        Attachment.builder()
            .card(card)
            .uploadedBy(user)
            .fileName(file.getOriginalFilename())
            .fileUrl(result.url())
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .publicId(result.publicId())
            .build();

    attachmentRepository.save(attachment);

    return mapToResponse(attachment);
  }

  public void delete(Integer id) {

    Attachment attachment =
        attachmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

    try {
      cloudinaryService.delete(attachment.getPublicId());
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Delete file on cloud failed", "DELETE-FAILED");
    }

    attachmentRepository.delete(attachment);
  }

  private AttachmentResponse mapToResponse(Attachment a) {
    return new AttachmentResponse(
        a.getId(),
        a.getFileName(),
        a.getFileUrl(),
        a.getFileSize(),
        a.getMimeType(),
        a.getIsCover());
  }
}
