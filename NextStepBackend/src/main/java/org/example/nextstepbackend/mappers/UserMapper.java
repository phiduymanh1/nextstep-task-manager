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

  @Mapping(source = "passwordHash", target = "password")
  LoginRequest toLoginRequest(User user);

  @Mapping(target = "passwordHash", ignore = true)
  User toUser(RegisterRequest request);

  @Mapping(source = "audit", target = "audit")
  UserResponse toUserResponse(User user);

  @Mapping(target = "username", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);
}
