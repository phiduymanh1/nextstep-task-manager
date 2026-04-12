import api from '@/api';

const baseUserUrl = '/attachments';

export const uploadAttachment = async (cardId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);

  const res = await api.post(
    `${baseUserUrl}/cards/${cardId}/attachments`,
    formData
  );

  return res.data.data;
};

export const deleteAttachment = async (id: number) => {
  await api.delete(`${baseUserUrl}/attachments/${id}`);
};
