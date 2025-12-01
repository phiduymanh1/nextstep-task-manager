package org.example.nextstepbackend.Dto.Request;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String fullName,
        String phone
) {}
