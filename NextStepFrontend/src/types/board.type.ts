import type { PageResponse } from "./api.type";
import type { ListsResponse } from "./lists.type";
import type { Visibility } from "./workspace.type";

export interface BoardResponse {
  id: number;
  name: string;
  slug: string;
  backgroundColor?: string;
  backgroundImageUrl?: string;
  isClosed: boolean;
}

export interface BoardDetailResponse {
  id: number;
  name: string;
  slug: string;
  backgroundColor?: string;
  backgroundImageUrl?: string;
  visibility: Visibility;
  lists: PageResponse<ListsResponse>;
}