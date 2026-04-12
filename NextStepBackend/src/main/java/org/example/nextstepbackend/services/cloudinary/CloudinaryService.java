package org.example.nextstepbackend.services.cloudinary;

import com.cloudinary.Cloudinary;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.response.cloud.UploadResult;
import org.example.nextstepbackend.exceptions.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

  private final Cloudinary cloudinary;

  private static final String PUBLIC_ID = "public_id";

  /**
   * Upload avatar to Cloudinary
   *
   * @param file MultipartFile
   * @param publicId public id
   * @return UploadResult
   */
  public UploadResult uploadAvatar(MultipartFile file, String publicId) {
    try {
      Map<String, Object> options =
          Map.of(
              "folder",
              "avatars",
              PUBLIC_ID,
              publicId,
              "overwrite",
              true,
              "resource_type",
              "image",
              "transformation",
              "w_256,h_256,c_fill,g_face");

      @SuppressWarnings("unchecked")
      Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

      return new UploadResult(
          result.get("secure_url").toString(), result.get(PUBLIC_ID).toString());

    } catch (Exception e) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Upload avatar failed", "UPLOAD-FAILED");
    }
  }

  public UploadResult uploadAttachment(MultipartFile file, String publicId) {
    try {
      Map<String, Object> options =
          Map.of(
              "folder",
              "attachments",
              PUBLIC_ID,
              publicId,
              "resource_type",
              "auto" // 🔥 hỗ trợ mọi file
              );

      Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

      return new UploadResult(
          result.get("secure_url").toString(), result.get(PUBLIC_ID).toString());

    } catch (Exception e) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Upload attachment failed", "UPLOAD-FAILED");
    }
  }

  public void delete(String publicId) {
    try {
      cloudinary.uploader().destroy(publicId, Map.of());
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Delete avatar failed", "DELETE-FAILED");
    }
  }
}
