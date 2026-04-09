import api from '@/api';

const baseUserUrl = '/card-member';

export const toggleCardMember = async (
  cardId: string,
  userId: number,
  assigned: boolean
) => {
  const endpoint = assigned
    ? `${baseUserUrl}/cards/${cardId}/unassign`
    : `${baseUserUrl}/cards/${cardId}/assign`;

  const res = await api.patch(endpoint, null, {
    params: { userId },
  });

  return res.data.data;
};
