import api from '@/api';
import type { ApiResponse, PageResponse } from '@/types/api.type';

const baseUrl = '/cards';

export interface Comment {
  id: string;
  userId: string;
  content: string;
  createdAt?: string;
}

export const getComments = async (cardId: string) => {
  const res = await api.get<ApiResponse<PageResponse<Comment>>>(
    `${baseUrl}/${cardId}/comments`
  );

  return res.data.data.items;
};