package org.example.nextstepbackend.mappers;

import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.entity.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkSpaceMapper {

    /**
     * Convert WorkSpaceRequest DTO to Workspace entity
     * @param workSpaceRequest the WorkSpaceRequest DTO
     * @return the Workspace entity
     */
    Workspace toWorkspace(WorkSpaceRequest workSpaceRequest);
}
