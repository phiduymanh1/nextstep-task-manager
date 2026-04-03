package org.example.nextstepbackend.dto.request;

public record AttachmentResponse(
    Integer id, String fileName, String fileUrl, Long fileSize, String mimeType, Boolean isCover) {}
