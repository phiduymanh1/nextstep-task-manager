package org.example.nextstepbackend.Mappers;

import org.example.nextstepbackend.Dto.Request.LoginRequest;
import org.example.nextstepbackend.Entitys.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "passwordHash", target = "password")
    LoginRequest toLoginRequest(User user);

}
