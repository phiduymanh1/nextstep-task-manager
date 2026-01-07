package org.example.nextstepbackend.validators;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AvatarValidator {

  private static final long MAX_SIZE = 2L * 1024 * 1024; // 2MB
  private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/jpg");

  public void validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Avatar file is required");
    }

    if (!ALLOWED_TYPES.contains(file.getContentType())) {
      throw new IllegalArgumentException("Only JPG, JPEG, PNG are allowed");
    }

    if (file.getSize() > MAX_SIZE) {
      throw new IllegalArgumentException("Avatar size must be <= 2MB");
    }
  }
}
