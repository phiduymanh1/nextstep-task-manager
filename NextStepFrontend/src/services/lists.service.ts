import api from '@/api';
import type { ListCreateSchema } from '@/schema/lists.schema';
import type { ApiResponse } from '@/types/api.type';
import type { ListDetailResponse } from '@/types/card.type';
import type {
  ListPositionRequest,
  ListsResponse,
  ListUpdateRequest,
} from '@/types/lists.type';

const baseUserUrl = '/lists';

export const createList = async (
  boardSlug: string,
  payload: ListCreateSchema
) => {
  const res = await api.post<ApiResponse<ListsResponse>>(
    `${baseUserUrl}/board/${boardSlug}`,
    payload
  );
  return res.data.data;
};

export const updateList = async (
  boardSlug: string,
  listId: string,
  payload: ListUpdateRequest
) => {
  const res = await api.patch(`${baseUserUrl}/${boardSlug}/${listId}`, payload);
  return res.data;
};

export const updateListPosition = async (
  boardSlug: string,
  listId: string,
  body: ListPositionRequest
) => {
  await api.patch(`${baseUserUrl}/${boardSlug}/${listId}/position`, body);
};

export const getListDetail = async (
  listId: string
): Promise<ListDetailResponse> => {
  const res = await api.get<ApiResponse<ListDetailResponse>>(
    `${baseUserUrl}/lists/${listId}`
  );
  return res.data.data;
};
