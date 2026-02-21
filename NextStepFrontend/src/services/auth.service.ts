import api from '@/api';
import type { ApiResponse } from '@/types/api.type';
import type { AuthResponse } from '@/types/auth.type';
import type { LoginFormValues } from '@/schema/auth.schema';

const baseAuthUrl = '/auth';

export const login = async (payload: LoginFormValues) => {
  const res = await api.post<ApiResponse<AuthResponse>>(`${baseAuthUrl}/login`, payload);

  return res.data.data;
};

export const refreshToken = async () => {
  const res = await api.post<ApiResponse<AuthResponse>>(`${baseAuthUrl}/refresh`);

  return res.data.data;
}

export const logout = async () => {
 const res = await api.post<ApiResponse<unknown>>(`${baseAuthUrl}/logout`); 

  return res.data.data;
}