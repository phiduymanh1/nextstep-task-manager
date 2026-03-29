package org.example.nextstepbackend.dto.response.board;

public record BoardResponse(
    Integer id,
    String name,
    String slug,
    String backgroundColor,
    String backgroundImageUrl,
    Boolean isClosed) {}
