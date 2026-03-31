import api from '@/api';
import type { ListCreateSchema } from '@/schema/lists.schema';
import type { ApiResponse } from '@/types/api.type';
import type { ListsResponse } from '@/types/lists.type';

const baseUserUrl = '/lists';

export const createList = async (boardSlug: string, payload: ListCreateSchema) => {
  const res = await api.post<ApiResponse<ListsResponse>>(
    `${baseUserUrl}/board/${boardSlug}`,
    payload
  );
  return res.data.data;
};
