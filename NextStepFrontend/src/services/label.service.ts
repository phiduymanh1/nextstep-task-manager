import api from '@/api';

const baseUserUrl = '/labels';

export const createBoardLabel = async (
  boardSlug: string,
  data: { name: string; color: string }
) => {
  const res = await api.post(`${baseUserUrl}/${boardSlug}`, data);
  return res.data.data;
};

export const toggleCardLabel = async (data: {
  cardId: number;
  labelId: number;
  selected: boolean;
}) => {
  const res = await api.post(`${baseUserUrl}/toggle`, data);
  return res.data;
};
