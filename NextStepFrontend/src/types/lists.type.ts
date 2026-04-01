export interface ListsResponse {
  id: number;
  name: string;
  isArchived: boolean;
}

export interface ListCreateRequest {
  name: string;
  beforeId: number | null;
  afterId: number | null;
}

export interface ListUpdateRequest {
  name: string;
}

export interface ListPositionRequest {
  beforeId: number | null;
  afterId: number | null;
}