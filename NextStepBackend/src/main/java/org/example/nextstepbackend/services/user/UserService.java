package org.example.nextstepbackend.services.user;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.UserUpdateRequest;
import org.example.nextstepbackend.dto.response.cloud.UploadResult;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.mappers.UserMapper;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.cloudinary.CloudinaryService;
import org.example.nextstepbackend.validators.AvatarValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final AuthService authService;
  private final CloudinaryService cloudinaryService;
  private final AvatarValidator avatarValidator;

  public UserResponse getUserMe(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    return userMapper.toUserResponse(user);
  }

  public UserResponse getUserById(Integer id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

    return userMapper.toUserResponse(user);
  }

  @Transactional
  public void updateUserMe(UserUpdateRequest request) {
    User user = authService.getCurrentUser();
    userMapper.updateUserFromRequest(request, user);
    userMapper.toUserResponse(user);
  }

  @Transactional
  public void updateAvatar(MultipartFile file) {
    Integer userId = authService.getCurrentUserId();

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    avatarValidator.validate(file);

    // Tạo publicId cố định cho user -> replace ảnh
    String publicId = "user_" + userId + "_avatar";

    UploadResult uploadResult = cloudinaryService.uploadAvatar(file, publicId);

    user.setAvatarUrl(uploadResult.url());
    user.setAvatarPublicId(uploadResult.publicId());

    userRepository.save(user);
  }
}
