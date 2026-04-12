package org.example.nextstepbackend.controller;

import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.AttachmentResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.attachment.AttachmentService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attachments")
public class AttachmentController extends BaseController {

  private final AttachmentService attachmentService;

  public AttachmentController(ApiResponseUtil responseUtil, AttachmentService attachmentService) {
    super(responseUtil);
    this.attachmentService = attachmentService;
  }

  @PostMapping("/cards/{cardId}/attachments")
  public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
      @PathVariable Integer cardId, @RequestParam("file") MultipartFile file) {
    AttachmentResponse response = attachmentService.upload(cardId, file);
    return ResponseEntity.ok(success(MessageConst.ATTACHMENT_UPLOAD_SUCCESS, response));
  }

  @DeleteMapping("/attachments/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Integer id) {
    attachmentService.delete(id);
    return ResponseEntity.ok(success(MessageConst.ATTACHMENT_UPLOAD_SUCCESS, null));
  }
}
