package org.example.nextstepbackend.Mappers;

import org.example.nextstepbackend.Dto.Request.LoginRequest;
import org.example.nextstepbackend.Dto.Request.RegisterRequest;
import org.example.nextstepbackend.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "passwordHash", target = "password")
    LoginRequest toLoginRequest(User user);

    @Mapping(target = "passwordHash", ignore = true)
    User toUser(RegisterRequest request);
}
