package org.example.nextstepbackend.mappers;

import java.util.List;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceDetailResponse;
import org.example.nextstepbackend.dto.response.Workspace.WorkspaceResponse;
import org.example.nextstepbackend.entity.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkSpaceMapper {

  /**
   * Convert WorkSpaceRequest DTO to Workspace entity
   *
   * @param workSpaceRequest the WorkSpaceRequest DTO
   * @return the Workspace entity
   */
  Workspace toWorkspace(WorkSpaceRequest workSpaceRequest);

  /**
   * Convert Workspace entity to WorkspaceResponse DTO
   *
   * @param workspace the Workspace entity
   * @return the WorkspaceResponse DTO
   */
  @Mapping(source = "audit", target = "audit")
  @Mapping(target = "createdById", source = "createdBy.id")
  @Mapping(target = "createdByName", source = "createdBy.username")
  WorkspaceResponse toWorkspaceResponse(Workspace workspace);

  /**
   * Convert a list of Workspace entities to a list of WorkspaceResponse DTOs
   *
   * @param workspaces the list of Workspace entities
   * @return the list of WorkspaceResponse DTOs
   */
  List<WorkspaceResponse> toWorkspaceResponseList(List<Workspace> workspaces);
}
