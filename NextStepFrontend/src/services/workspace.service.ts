import api from '@/api';
import type { workspaceSchema } from '@/schema/workspace.schema';
import type { ApiResponse } from '@/types/api.type';
import type {
  Workspace,
  WorkspaceDetailResponse,
} from '@/types/workspace.type';

const baseUserUrl = '/work-space';

export const getWorkspaceMe = async () => {
  const res = await api.get<ApiResponse<Workspace[]>>(`${baseUserUrl}/me`);

  return res.data.data;
};

export const createWorkspace = async (payload: workspaceSchema) => {
  const res = await api.post<ApiResponse<Workspace>>(
    `${baseUserUrl}/me`,
    payload
  );

  return res.data.data;
};

export const getWorkspaceDetailBySlug = async (
  slug: string,
  params?: { page?: number; size?: number }
) => {
  const res = await api.get<ApiResponse<WorkspaceDetailResponse>>(
    `${baseUserUrl}/boards/${slug}`,
    { params }
  );

  return res.data.data;
};

export const updateWorkspace = async (
  slug: string,
  payload: workspaceSchema
) => {
  const res = await api.patch<ApiResponse<Workspace>>(
    `${baseUserUrl}/me/${slug}`,
    payload
  );

  return res.data.data;
};
