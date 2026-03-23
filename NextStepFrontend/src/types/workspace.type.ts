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


