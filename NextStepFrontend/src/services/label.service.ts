import api from '@/api';

export const createBoardLabel = async (
  boardSlug: string,
  data: { name: string; color: string }
) => {
  const res = await api.post(`/labels/${boardSlug}`, data);
  return res.data.data;
};
