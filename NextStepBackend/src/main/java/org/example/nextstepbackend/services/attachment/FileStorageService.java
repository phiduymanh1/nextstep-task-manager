package org.example.nextstepbackend.services.attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  public String save(MultipartFile file) throws IOException {
    String uploadDir = "uploads/";

    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

    Path path = Paths.get(uploadDir + fileName);

    Files.copy(file.getInputStream(), path);

    return "/uploads/" + fileName;
  }
}
