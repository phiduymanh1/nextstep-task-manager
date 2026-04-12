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

export const getComments = async (
  cardId: string,
  params: { page: number; size: number }
): Promise<PageResponse<Comment>> => {
  const res = await api.get<ApiResponse<PageResponse<Comment>>>(
    `${baseUrl}/${cardId}/comments`,
    { params }
  );

  return res.data.data;
};

export const createComment = async (
  cardId: number,
  data: CommentRequest
): Promise<CommentResponse> => {
  const res = await api.post(`/comment/${cardId}`, data);
  return res.data.data;
};
