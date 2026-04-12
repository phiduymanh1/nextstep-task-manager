import api from '@/api';
import type { ApiResponse } from '@/types/api.type';

const baseUserUrl = '/board-member';

export type BoardRole = 'ADMIN' | 'MEMBER' | 'OBSERVER';

export interface BoardMemberResponse {
  userId: number;
  fullName: string;
  username?: string;
  email?: string;
  role: BoardRole;
  avatarUrl?: string;
}

export interface AddBoardMemberRequest {
  userId: number;
  role: BoardRole;
}

export const getBoardMembers = async (
  boardSlug: string
): Promise<BoardMemberResponse[]> => {
  const res = await api.get<ApiResponse<BoardMemberResponse[]>>(
    `${baseUserUrl}/boards/${boardSlug}/members`
  );

  return res.data.data;
};

export const addMemberToBoard = async (
  boardSlug: string,
  payload: AddBoardMemberRequest
): Promise<void> => {
  await api.post<ApiResponse<null>>(
    `${baseUserUrl}/boards/${boardSlug}/members`,
    payload
  );
};

export const removeMemberFromBoard = async (
  boardSlug: string,
  userId: number
): Promise<void> => {
  await api.delete<ApiResponse<null>>(
    `${baseUserUrl}/boards/${boardSlug}/members/${userId}`
  );
};
