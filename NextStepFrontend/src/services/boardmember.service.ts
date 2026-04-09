import api from '@/api';
import type { ApiResponse } from '@/types/api.type';

const baseUserUrl = '/board-member';

interface BoardMember {
  userId: number;
  fullName: string;
  avatarUrl?: string;
  role: string;
}

export const getBoardMembers = async (boardId: string) => {
  const res = await api.get<ApiResponse<BoardMember[]>>(
    `${baseUserUrl}/boards/${boardId}/members`
  );

  return res.data.data;
};