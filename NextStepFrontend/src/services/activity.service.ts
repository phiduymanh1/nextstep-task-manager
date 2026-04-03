import api from '@/api';
import type { ApiResponse, PageResponse } from '@/types/api.type';

const baseUrl = '/cards';

export interface Activity {
  id: string;
  message: string;
  createdAt?: string;
}

export const getActivities = async (cardId: string): Promise<Activity[]> => {
  const res = await api.get<ApiResponse<PageResponse<Activity>>>(
    `${baseUrl}/${cardId}/activities`
  );

  return res.data.data.items;
};
