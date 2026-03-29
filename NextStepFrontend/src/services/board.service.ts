import api from '@/api';
import type { BoardSchema } from '@/schema/board.schema';
import type { ApiResponse } from '@/types/api.type';
import type { BoardResponse } from '@/types/board.type';

const baseUserUrl = '/board';

export const createBoard = async (workspaceSlug: string, payload: BoardSchema) => {
  const res = await api.post<ApiResponse<BoardResponse>>(
    `${baseUserUrl}/${workspaceSlug}`,
    payload
  );

  return res.data.data;
};
