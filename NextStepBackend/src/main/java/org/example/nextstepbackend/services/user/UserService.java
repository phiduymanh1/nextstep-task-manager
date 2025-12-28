package org.example.nextstepbackend.services.user;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.mappers.UserMapper;
import org.example.nextstepbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserResponse getUserMe(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    return userMapper.toUserResponse(user);
  }
}
