import api from '@/api';
import type { CardCreateRequest, CardPositionRequest } from '@/types/card.type';

const baseUserUrl = '/cards';

export const createCard = async (
  listId: string,
  payload: CardCreateRequest
): Promise<{ id: number; title: string; description?: string }> => {
  const res = await api.post(`${baseUserUrl}/lists/${listId}/cards`, payload);
  return res.data.data;
};

export const updateCardPosition = async (
  cardId: string,
  body: CardPositionRequest
) => {
  await api.patch(`${baseUserUrl}/${cardId}/position`, body);
};