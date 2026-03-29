import type { PageResponse } from "./api.type";
import type { BoardResponse } from "./board.type";

export interface AuditRecord {
  createdAt: string;
  updatedAt: string;
}

export type Visibility = 'PUBLIC' | 'PRIVATE' | 'WORKSPACE';

export interface Workspace {
    id: number;
    name: string;
    slug: string;
    description: string;
    visibility: Visibility;
    createdById: string;
    createdByName: string;
    audit: AuditRecord;
}

export interface WorkspaceDetailResponse {
  id: number;
  name: string;
  slug: string;
  visibility: Visibility;
  boards: PageResponse<BoardResponse>;
};


