import api from '@/api';
import type { BoardSchema, BoardUpdateSchema } from '@/schema/board.schema';
import type { ApiResponse } from '@/types/api.type';
import type { BoardDetailResponse, BoardResponse } from '@/types/board.type';

const baseUserUrl = '/board';

export const createBoard = async (
  workspaceSlug: string,
  payload: BoardSchema
) => {
  const res = await api.post<ApiResponse<BoardResponse>>(
    `${baseUserUrl}/${workspaceSlug}`,
    payload
  );

  return res.data.data;
};

export const getBoardDetail = async (
  slug: string,
  params?: { page?: number; size?: number }
) => {
  const res = await api.get<ApiResponse<BoardDetailResponse>>(
    `${baseUserUrl}/${slug}`,
    { params }
  );

  return res.data.data;
};

export const updateBoard = async (slug: string, payload: BoardUpdateSchema) => {
  const res = await api.patch<ApiResponse<BoardDetailResponse>>(
    `${baseUserUrl}/${slug}`,
    payload
  );

  return res.data.data;
};