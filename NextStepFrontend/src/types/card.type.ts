export type Visibility = 'PRIVATE' | 'PUBLIC' | 'WORKSPACE';

// ===== Domain Types =====
export interface Card {
  id: string;
  title: string;
  description?: string;
  isCompleted?: boolean;
}

export interface Column {
  id: string;
  title: string;
  cards: Card[];
  loading?: boolean;
  error?: string | null;
}

export interface BoardState {
  name: string;
  visibility: Visibility;
  backgroundColor?: string;
  backgroundImageUrl?: string;
}

// ===== API Request/Response Types =====
export interface ListUpdateRequest {
  name: string;
}

export interface ListPositionRequest {
  beforeId: number | null;
  afterId: number | null;
}

export interface CardCreateRequest {
  title: string;
  description?: string;
  afterId?: number | null;
  beforeId?: number | null;
}

export interface CardPositionRequest {
  listId: number;
  afterId?: number | null;
  beforeId?: number | null;
}

export interface CardResponse {
  id: number;
  title: string;
  description?: string;
  position: number;
  isCompleted: boolean;
  isArchived: boolean;
  dueDate?: string;
}
export interface ListDetailResponse {
  id: number;
  name: string;
  position: number;
  cards: CardResponse[];
}

// ===== Visibility Config =====
export const VISIBILITY_OPTIONS: {
  value: Visibility;
  label: string;
  icon: string;
  desc: string;
}[] = [
  {
    value: 'PRIVATE',
    label: 'Riêng tư',
    icon: '🔒',
    desc: 'Chỉ bạn và thành viên được mời mới có thể xem bảng này.',
  },
  {
    value: 'WORKSPACE',
    label: 'Workspace',
    icon: '👥',
    desc: 'Tất cả thành viên trong workspace đều có thể xem bảng này.',
  },
  {
    value: 'PUBLIC',
    label: 'Công khai',
    icon: '🌐',
    desc: 'Bất kỳ ai trên internet đều có thể xem bảng này.',
  },
];

export interface CardData {
  id: string;
  title: string;
  description: string;
  isCompleted: boolean;
  dueDate: string | null;
  dueReminder: boolean;
  labels: {
    selectedLabelIds: string[];
    boardLabels: BoardLabel[];
  };
  checklists: Checklist[];
  attachments: Attachment[];
}

export interface BoardLabel {
  id: string;
  name: string;
  color: string;
}

export interface ChecklistItem {
  id: string;
  content: string;
  isCompleted: boolean;
}

export interface Checklist {
  id: string;
  title: string;
  items: ChecklistItem[];
}

export interface Attachment {
  id: string;
  fileName: string;
  fileUrl: string;
}
