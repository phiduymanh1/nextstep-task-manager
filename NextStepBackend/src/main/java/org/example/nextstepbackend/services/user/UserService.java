package org.example.nextstepbackend.services.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.UserSearchResponse;
import org.example.nextstepbackend.dto.request.UserUpdateRequest;
import org.example.nextstepbackend.dto.response.cloud.UploadResult;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.exceptions.ResourceNotFoundException;
import org.example.nextstepbackend.mappers.UserMapper;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.auth.AuthService;
import org.example.nextstepbackend.services.cloudinary.CloudinaryService;
import org.example.nextstepbackend.validators.AvatarValidator;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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

  /**
   * Get current user info
   *
   * @param email user email
   * @return UserResponse
   */
  public UserResponse getUserMe(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Unauthorized"));

    return userMapper.toUserResponse(user);
  }

  /**
   * Get user info by id
   *
   * @param id user id
   * @return UserResponse
   */
  public UserResponse getUserById(Integer id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    return userMapper.toUserResponse(user);
  }

  /**
   * Update current user info
   *
   * @param request UserUpdateRequest
   */
  @Transactional
  public void updateUserMe(UserUpdateRequest request) {
    User user = authService.getCurrentUser();
    userMapper.updateUserFromRequest(request, user);
    userMapper.toUserResponse(user);
  }

  /**
   * Update current user avatar
   *
   * @param file MultipartFile
   */
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

  public List<UserSearchResponse> searchUser(String keyword, String workspaceSlug) {
    return userRepository.searchUser(keyword, workspaceSlug);
  }
}
