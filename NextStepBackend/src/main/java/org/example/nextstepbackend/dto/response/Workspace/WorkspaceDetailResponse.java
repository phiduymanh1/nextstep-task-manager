package org.example.nextstepbackend.dto.response.Workspace;

import org.example.nextstepbackend.dto.response.board.BoardResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.enums.Visibility;

public record WorkspaceDetailResponse(
        Integer id, String name, String slug, Visibility visibility, PageResponse<BoardResponse> boards) {}
