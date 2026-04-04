import api from '@/api';
import type { ApiResponse, PageResponse } from '@/types/api.type';

const baseUrl = '/cards';

export interface Comment {
  id: number;
  userId: number;
  userName: string;
  avatarUrl?: string;
  content: string;
  createdAt?: string;
}
export interface CommentRequest {
  content: string;
}

export interface CommentResponse {
  id: number;
  userId: number;
  userName: string;
  avatarUrl?: string;
  content: string;
  createdAt?: string;
}

export const getComments = async (cardId: string) => {
  const res = await api.get<ApiResponse<PageResponse<Comment>>>(
    `${baseUrl}/${cardId}/comments`
  );

  return res.data.data.items;
};

export const createComment = async (
  cardId: number,
  data: CommentRequest
): Promise<CommentResponse> => {
  const res = await api.post(`/comment/${cardId}`, data);
  return res.data.data;
};
