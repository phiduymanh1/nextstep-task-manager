import api from '@/api';

export interface ChecklistRequest {
  title: string;
  afterId?: number | null;
  beforeId?: number | null;
}

export interface ChecklistItemResponse {
  id: number;
  content: string;
  isDone: boolean;
}

export interface ChecklistResponse {
  id: number;
  title: string;
  position: number;
  items: ChecklistItemResponse[];
}

export interface ChecklistItemRequest {
  content: string;
  position?: number;
  dueDate?: string | null; 
  afterId?: number | null;
  beforeId?: number | null;
}

export interface ChecklistItemResponse {
  id: number;
  content: string;
  isDone: boolean;
  position?: number;
  dueDate?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export const createChecklist = async (
  cardId: number,
  data: ChecklistRequest
): Promise<ChecklistResponse> => {
  const res = await api.post(`/checklists/${cardId}`, data);
  return res.data.data;
};


export const createChecklistItem = async (
  checklistId: number,
  data: ChecklistItemRequest
): Promise<ChecklistItemResponse> => {
  const res = await api.post(`/checklists/${checklistId}/items`, data);

  return res.data.data;
};

