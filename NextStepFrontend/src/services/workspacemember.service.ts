import api from '@/api';
import type { WorkspaceRole } from '@/pages/workspace/Workspacemembersmodal';
import type { ApiResponse } from '@/types/api.type';

const baseUserUrl = '/workspace-member';

export interface WorkspaceMemberResponse {
  userId: number;
  fullName: string;
  email: string;
  avatarUrl?: string;

  username?: string;
  role?: WorkspaceRole;
}

export const getWorkspaceMembers = async (
  workspaceSlug: string
): Promise<WorkspaceMemberResponse[]> => {
  const res = await api.get<ApiResponse<WorkspaceMemberResponse[]>>(
    `${baseUserUrl}/${workspaceSlug}/members`
  );

  return res.data.data;
};

export const addWorkspaceMember = async (
  slug: string,
  userId: number,
  role: WorkspaceRole
): Promise<void> => {
  await api.post(`${baseUserUrl}/workspaces/${slug}/members`, null, {
    params: { userId, role },
  });
};

export const removeWorkspaceMember = async (
  slug: string,
  userId: number
): Promise<void> => {
  await api.delete(`${baseUserUrl}/workspaces/${slug}/members/${userId}`);
};

export const updateMemberRole = async (
  slug: string,
  userId: number,
  role: WorkspaceRole
): Promise<void> => {
  await api.patch(`${baseUserUrl}/workspaces/${slug}/members/${userId}/role`, {
    role,
  });
};
