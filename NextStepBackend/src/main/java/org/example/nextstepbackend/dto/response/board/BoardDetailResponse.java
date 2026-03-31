package org.example.nextstepbackend.dto.response.board;

import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.dto.response.lists.ListsResponse;
import org.example.nextstepbackend.enums.Visibility;

public record BoardDetailResponse(
    Integer id,
    String name,
    String slug,
    String backgroundColor,
    String backgroundImageUrl,
    Visibility visibility,
    PageResponse<ListsResponse> lists) {}
