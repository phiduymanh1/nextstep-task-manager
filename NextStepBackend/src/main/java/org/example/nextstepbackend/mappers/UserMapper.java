package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.dto.request.RegisterRequest;
import org.example.nextstepbackend.dto.request.UserUpdateRequest;
import org.example.nextstepbackend.dto.response.user.UserResponse;
import org.example.nextstepbackend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  /**
   * Convert User entity to LoginRequest DTO
   *
   * @param user the User entity
   * @return the LoginRequest DTO
   */
  @Mapping(source = "passwordHash", target = "password")
  LoginRequest toLoginRequest(User user);

  /**
   * Convert RegisterRequest DTO to User entity
   *
   * @param request the RegisterRequest DTO
   * @return the User entity
   */
  @Mapping(target = "passwordHash", ignore = true)
  User toUser(RegisterRequest request);

  /**
   * Convert User entity to UserResponse DTO
   *
   * @param user the User entity
   * @return the UserResponse DTO
   */
  @Mapping(source = "audit", target = "audit")
  UserResponse toUserResponse(User user);

  /**
   * Update User entity from UserUpdateRequest DTO
   *
   * @param request the UserUpdateRequest DTO
   * @param user the User entity to be updated
   */
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);
}
