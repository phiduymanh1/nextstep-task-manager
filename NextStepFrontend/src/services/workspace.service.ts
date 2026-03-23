import api from '@/api';
import type { ApiResponse } from '@/types/api.type';
import type { Workspace } from '@/types/workspace.type';

const baseUserUrl = '/work-space';

export const getWorkspaceMe = async () => {
  const res = await api.get<ApiResponse<Workspace[]>>(`${baseUserUrl}/me`);

  return res.data.data;
};
