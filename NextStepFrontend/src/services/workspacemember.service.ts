import api from '@/api';
import type { ApiResponse } from '@/types/api.type';

const baseUserUrl = '/workspace-member';

export interface WorkspaceMemberResponse {
  userId: number;
  fullName: string;
  email: string;
  avatar?: string;

  username?: string;
  workspaceRole?: string;
}

export const getWorkspaceMembers = async (
  workspaceSlug: string
): Promise<WorkspaceMemberResponse[]> => {
  const res = await api.get<ApiResponse<WorkspaceMemberResponse[]>>(
    `${baseUserUrl}/${workspaceSlug}/members`
  );

  return res.data.data;
};
