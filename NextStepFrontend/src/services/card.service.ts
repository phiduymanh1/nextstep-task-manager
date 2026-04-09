import api from '@/api';
import type { ApiResponse } from '@/types/api.type';
import type {
  CardCreateRequest,
  CardData,
  CardPositionRequest,
} from '@/types/card.type';

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

export const getCardDetail = async (cardId: string): Promise<CardData> => {
  const res = await api.get<ApiResponse<CardData>>(
    `${baseUserUrl}/${cardId}/detail`
  );
  return res.data.data;
};

export interface UpdateCardPayload {
  title?: string;
  description?: string;
  dueDate?: string | null;
  dueReminder?: boolean;
  isCompleted?: boolean;
  coverColor?: string;
  coverImageUrl?: string;
}

export const updateCard = async (
  cardId: string,
  payload: UpdateCardPayload
) => {
  const res = await api.patch<ApiResponse<unknown>>(
    `${baseUserUrl}/${cardId}`,
    payload
  );
  return res.data.data;
};

export const archiveCard = async (cardId: number) => {
  const res = await api.delete<ApiResponse<null>>(`${baseUserUrl}/${cardId}`);
  return res.data;
};
