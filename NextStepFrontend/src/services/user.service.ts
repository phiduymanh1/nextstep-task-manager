import api from '@/api';
import type { ApiResponse } from '@/types/api.type';
import type { User } from '@/types/user.type';

const baseUserUrl = '/users';

export const getMe = async () => {
  const res = await api.get<ApiResponse<User>>(`${baseUserUrl}/me`);

  return res.data.data;
};
