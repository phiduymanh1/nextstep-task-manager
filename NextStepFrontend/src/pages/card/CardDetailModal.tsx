import React, { useEffect, useState, useRef } from 'react';
import '@/assets/styles/CardDetailModal.css';
import {
  getCardDetail,
  updateCard,
  archiveCard,
} from '@/services/card.service';
import {
  getComments,
  createComment,
  type Comment,
} from '@/services/comment.service';
import { getActivities } from '@/services/activity.service';
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  verticalListSortingStrategy,
  useSortable,
  arrayMove,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { createBoardLabel, toggleCardLabel } from '@/services/label.service';
import { useParams } from 'react-router-dom';
import {
  createChecklist,
  createChecklistItem,
  toggleChecklistItem,
  deleteChecklist,
  deleteChecklistItem,
} from '@/services/checklist.service';
import {
  deleteAttachment,
  uploadAttachment,
} from '@/services/attachment.service';

// ============================================================
// 1. TYPES
// ============================================================
interface Label {
  id: string;
  name: string;
  color: string;
}

interface ChecklistItem {
  id: string;
  content: string;
  isCompleted: boolean;
}

interface Checklist {
  id: string;
  title: string;
  position?: number;
  items: ChecklistItem[];
}

interface Attachment {
  id: string;
  fileName: string;
  fileUrl: string;
  fileSize?: number;
  mimeType?: string;
  isCover?: boolean;
}

interface Activity {
  id: string;
  message: string;
  createdAt?: string;
}

interface CardData {
  id: string;
  title: string;
  description: string;
  isCompleted: boolean;
  dueDate: string | null;
  dueReminder: boolean;
  labels: {
    selectedLabelIds: string[];
    boardLabels: Label[];
  };
  checklists: Checklist[];
  attachments: Attachment[];
}

interface Props {
  cardId: string;
  onClose: () => void;
  onToggleComplete: (cardId: string, isCompleted: boolean) => void;
  /** Yêu cầu 3: callback trả về card state mới nhất khi đóng modal */
  onCardUpdated?: (cardId: string, updatedCard: Partial<CardData>) => void;
  /** Yêu cầu 4: callback khi card bị xóa */
  onCardDeleted?: (cardId: string) => void;
}

// ============================================================
// 2. SORTABLE CHECKLIST ITEM
// ============================================================
function SortableChecklistItem({
  item,
  onToggle,
  onDelete,
}: {
  item: ChecklistItem;
  onToggle: (id: string) => void;
  onDelete: (id: string) => void;
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: item.id });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  };

  return (
    <label ref={setNodeRef} style={style} className="cdm-checklist-item">
      <span className="cdm-drag-handle" {...listeners} {...attributes}>
        ⠿
      </span>
      <input
        type="checkbox"
        checked={item.isCompleted}
        onChange={() => onToggle(item.id)}
      />
      <span className={item.isCompleted ? 'done' : ''}>{item.content}</span>

      {/* Yêu cầu 1: nút xóa checklist item */}
      <button
        className="cdm-checklist-item-delete"
        title="Xóa mục"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          onDelete(item.id);
        }}
      >
        ✕
      </button>
    </label>
  );
}

// ============================================================
// 3. MODAL OVERLAY WRAPPER
// ============================================================
function ModalOverlay({
  children,
  onClose,
}: {
  children: React.ReactNode;
  onClose: () => void;
}) {
  return (
    <div className="cdm-inner-overlay" onClick={onClose}>
      <div className="cdm-inner-modal" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>
  );
}

// ============================================================
// 4. LABEL MODAL
// ============================================================
function LabelModal({
  boardLabels,
  selectedIds,
  onToggle,
  onClose,
  onAddLabel,
  boardSlug,
}: {
  boardLabels: Label[];
  selectedIds: string[];
  onToggle: (id: string) => void;
  onClose: () => void;
  onAddLabel?: (label: Label) => void;
  boardSlug: string;
}) {
  const [creating, setCreating] = useState(false);
  const [name, setName] = useState('');
  const [color, setColor] = useState('#579dff');
  const [loading, setLoading] = useState(false);

  const handleCreate = async () => {
    if (!name.trim()) return;
    try {
      setLoading(true);
      const newLabel = await createBoardLabel(boardSlug, {
        name: name.trim(),
        color,
      });
      onAddLabel?.(newLabel);
      setName('');
      setCreating(false);
    } catch (err) {
      console.error('Create label error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ModalOverlay onClose={onClose}>
      <div className="cdm-inner-modal-header">
        <span>Nhãn</span>
        <button className="cdm-icon-btn" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="cdm-inner-modal-body">
        {boardLabels.length === 0 && (
          <p className="cdm-empty-hint">Chưa có nhãn nào trong bảng</p>
        )}
        {boardLabels.map((l) => {
          const checked = selectedIds.includes(l.id);
          return (
            <div
              key={l.id}
              className="cdm-label-option"
              onClick={() => onToggle(l.id)}
            >
              <span
                className="cdm-label-color-pill"
                style={{ background: l.color }}
              >
                {l.name}
              </span>
              <input
                type="checkbox"
                className="cdm-label-checkbox"
                checked={checked}
                readOnly
              />
            </div>
          );
        })}
        {!creating ? (
          <button
            className="cdm-add-inline-btn"
            style={{ marginTop: '10px' }}
            onClick={() => setCreating(true)}
          >
            + Tạo nhãn mới
          </button>
        ) : (
          <div style={{ marginTop: '10px' }}>
            <input
              className="cdm-inner-input"
              placeholder="Tên nhãn..."
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <input
              type="color"
              value={color}
              onChange={(e) => setColor(e.target.value)}
              style={{ marginTop: '6px', width: '100%', height: '36px' }}
            />
            <div style={{ display: 'flex', gap: '6px', marginTop: '6px' }}>
              <button
                className="cdm-comment-submit"
                onClick={handleCreate}
                disabled={loading}
              >
                {loading ? 'Đang tạo...' : 'Tạo'}
              </button>
              <button
                className="cdm-comment-cancel"
                onClick={() => setCreating(false)}
              >
                Hủy
              </button>
            </div>
          </div>
        )}
      </div>
    </ModalOverlay>
  );
}

// ============================================================
// 5. DATE MODAL
// ============================================================
function DateModal({
  dueDate,
  dueReminder,
  onSave,
  onClose,
}: {
  dueDate: string | null;
  dueReminder: boolean;
  onSave: (date: string | null, reminder: boolean) => void;
  onClose: () => void;
}) {
  const toInputValue = (d: string | null) => {
    if (!d) return '';
    const dt = new Date(d);
    if (isNaN(dt.getTime())) return '';
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}T${pad(dt.getHours())}:${pad(dt.getMinutes())}`;
  };

  const [dateVal, setDateVal] = useState(toInputValue(dueDate));
  const [reminder, setReminder] = useState(dueReminder);

  const handleSave = () => {
    onSave(dateVal ? new Date(dateVal).toISOString() : null, reminder);
    onClose();
  };

  const handleClear = () => {
    onSave(null, false);
    onClose();
  };

  return (
    <ModalOverlay onClose={onClose}>
      <div className="cdm-inner-modal-header">
        <span>Ngày đến hạn</span>
        <button className="cdm-icon-btn" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="cdm-inner-modal-body">
        <p className="cdm-field-label">Ngày & giờ</p>
        <input
          type="datetime-local"
          className="cdm-datepicker-input"
          value={dateVal}
          onChange={(e) => setDateVal(e.target.value)}
        />
        <label className="cdm-reminder-check" style={{ marginBottom: '16px' }}>
          <input
            type="checkbox"
            checked={reminder}
            onChange={(e) => setReminder(e.target.checked)}
          />
          Nhắc nhở trước khi đến hạn
        </label>
        <div className="cdm-datepicker-actions">
          {dueDate && (
            <button className="cdm-datepicker-clear" onClick={handleClear}>
              Xóa ngày
            </button>
          )}
          <button className="cdm-datepicker-confirm" onClick={handleSave}>
            Lưu
          </button>
        </div>
      </div>
    </ModalOverlay>
  );
}

// ============================================================
// 6. CHECKLIST MODAL
// ============================================================
function ChecklistModal({
  cardId,
  onAdd,
  onClose,
  onCreated,
}: {
  cardId: number;
  onAdd: (checklist: Checklist) => void;
  onClose: () => void;
  onCreated?: () => void;
}) {
  const [title, setTitle] = useState('Việc cần làm');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!title.trim()) return;
    try {
      setLoading(true);
      const res = await createChecklist(cardId, { title: title.trim() });
      const mapped: Checklist = {
        id: String(res.id),
        title: res.title,
        position: Number(res.position),
        items: (res.items || []).map((item) => ({
          id: String(item.id),
          content: item.content,
          isCompleted: item.isDone,
        })),
      };
      onAdd(mapped);
      onCreated?.();
      onClose();
    } catch (err) {
      console.error('Create checklist error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ModalOverlay onClose={onClose}>
      <div className="cdm-inner-modal-header">
        <span>Thêm checklist</span>
        <button className="cdm-icon-btn" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="cdm-inner-modal-body">
        <p className="cdm-field-label">Tiêu đề</p>
        <input
          className="cdm-inner-input"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSubmit();
          }}
          autoFocus
        />
        <button
          className="cdm-datepicker-confirm"
          style={{ marginTop: '10px', width: '100%' }}
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? 'Đang thêm...' : 'Thêm'}
        </button>
      </div>
    </ModalOverlay>
  );
}

// ============================================================
// 7. ATTACHMENT MODAL
// ============================================================
function AttachmentModal({
  onAttach,
  onClose,
}: {
  onAttach: (file: File) => void;
  onClose: () => void;
}) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);

  const handleFiles = (files: FileList | null) => {
    if (files && files[0]) {
      onAttach(files[0]);
      onClose();
    }
  };

  return (
    <ModalOverlay onClose={onClose}>
      <div className="cdm-inner-modal-header">
        <span>Đính kèm tệp</span>
        <button className="cdm-icon-btn" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="cdm-inner-modal-body">
        <div
          className={`cdm-dropzone ${dragging ? 'dragging' : ''}`}
          onDragOver={(e) => {
            e.preventDefault();
            setDragging(true);
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={(e) => {
            e.preventDefault();
            setDragging(false);
            handleFiles(e.dataTransfer.files);
          }}
          onClick={() => fileRef.current?.click()}
        >
          <span className="cdm-dropzone-icon">📎</span>
          <span>Kéo thả hoặc click để chọn tệp</span>
          <span style={{ fontSize: '12px', color: 'rgba(255,255,255,0.3)' }}>
            Hỗ trợ mọi định dạng
          </span>
        </div>
        <input
          ref={fileRef}
          type="file"
          style={{ display: 'none' }}
          onChange={(e) => handleFiles(e.target.files)}
        />
      </div>
    </ModalOverlay>
  );
}

// ============================================================
// 8. ADD BUTTON MODAL
// ============================================================
type AddModalType = 'label' | 'date' | 'checklist' | 'attachment' | null;

function AddMenuModal({
  onSelect,
  onClose,
}: {
  onSelect: (type: AddModalType) => void;
  onClose: () => void;
}) {
  const items = [
    { type: 'label' as AddModalType, icon: '🏷', label: 'Nhãn' },
    { type: 'date' as AddModalType, icon: '🕐', label: 'Ngày' },
    { type: 'checklist' as AddModalType, icon: '☑', label: 'Việc cần làm' },
    { type: 'attachment' as AddModalType, icon: '📎', label: 'Đính kèm' },
  ];

  return (
    <ModalOverlay onClose={onClose}>
      <div className="cdm-inner-modal-header">
        <span>Thêm vào thẻ</span>
        <button className="cdm-icon-btn" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="cdm-inner-modal-body" style={{ padding: '8px' }}>
        {items.map((item) => (
          <button
            key={item.type}
            className="cdm-add-menu-item"
            onClick={() => onSelect(item.type)}
          >
            <span style={{ fontSize: '16px' }}>{item.icon}</span>
            {item.label}
          </button>
        ))}
      </div>
    </ModalOverlay>
  );
}

// ============================================================
// 9. MAIN COMPONENT
// ============================================================
export default function CardDetailModal({
  cardId,
  onClose,
  onToggleComplete,
  onCardUpdated,
  onCardDeleted,
}: Props) {
  const { boardSlug } = useParams<{ boardSlug: string }>();
  const [loading, setLoading] = useState(true);
  const [card, setCard] = useState<CardData | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentPage, setCommentPage] = useState(0);
  const [commentHasMore, setCommentHasMore] = useState(true);

  const [activities, setActivities] = useState<Activity[]>([]);
  const [activityPage, setActivityPage] = useState(0);
  const [activityHasMore, setActivityHasMore] = useState(true);

  const [commentInput, setCommentInput] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [deletingCard, setDeletingCard] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [activeModal, setActiveModal] = useState<AddModalType | 'addMenu'>(
    null
  );

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );

  // ===== Load data =====
  useEffect(() => {
    setComments([]);
    setActivities([]);
    setCommentPage(0);
    setActivityPage(0);
    setCommentHasMore(true);
    setActivityHasMore(true);

    const loadData = async () => {
      setLoading(true);
      try {
        const detail = await getCardDetail(cardId);
        setCard(detail);

        await Promise.all([loadComments(0), loadActivities(0)]);
      } catch (error) {
        console.error('Lỗi load card:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [cardId]);

  const loadActivities = async (page = 0) => {
    try {
      const res = await getActivities(cardId, { page, size: 10 });

      setActivities((prev) => {
        if (page === 0) return res.items;

        const ids = new Set(prev.map((i) => i.id));
        const newItems = res.items.filter((i) => !ids.has(i.id));

        return [...prev, ...newItems];
      });

      setActivityPage(res.page);
      setActivityHasMore(res.page < res.totalPages - 1);
    } catch (err) {
      console.error('Load activities error', err);
    }
  };

  const loadComments = async (page = 0) => {
    try {
      const res = await getComments(cardId, { page, size: 10 });

      setComments((prev) => {
        if (page === 0) return res.items;

        // tránh duplicate
        const ids = new Set(prev.map((i) => i.id));
        const newItems = res.items.filter((i) => !ids.has(i.id));

        return [...prev, ...newItems];
      });

      setCommentPage(res.page);
      setCommentHasMore(res.page < res.totalPages - 1);
    } catch (err) {
      console.error('Load comments error', err);
    }
  };

  const closeModal = () => setActiveModal(null);
  const openModal = (type: AddModalType | 'addMenu') => setActiveModal(type);

  // ===== Yêu cầu 3: đóng modal và trả state mới nhất ra ngoài =====
  const handleClose = () => {
    if (card) {
      onCardUpdated?.(cardId, {
        title: card.title,
        description: card.description,
        isCompleted: card.isCompleted,
        dueDate: card.dueDate,
        dueReminder: card.dueReminder,
        labels: card.labels,
        checklists: card.checklists,
        attachments: card.attachments,
      });
    }
    onClose();
  };

  // ===== Handlers =====
  const handleUpdateTitle = async (e: React.FocusEvent<HTMLHeadingElement>) => {
    const newTitle = e.currentTarget.textContent?.trim();
    if (newTitle && newTitle !== card?.title) {
      await updateCard(cardId, { title: newTitle });
      setCard((prev) => (prev ? { ...prev, title: newTitle } : null));
    }
  };

  const handleUpdateDescription = async (
    e: React.FocusEvent<HTMLTextAreaElement>
  ) => {
    const newDesc = e.target.value;
    if (newDesc !== card?.description) {
      await updateCard(cardId, { description: newDesc });
      setCard((prev) => (prev ? { ...prev, description: newDesc } : null));
    }
  };

  const handleToggleComplete = async () => {
    if (!card) return;
    const newVal = !card.isCompleted;
    await updateCard(cardId, { isCompleted: newVal });
    setCard((prev) => (prev ? { ...prev, isCompleted: newVal } : null));
    onToggleComplete(cardId, newVal);
  };

  // Label toggle
  const handleToggleLabel = async (labelId: string) => {
    if (!card) return;
    const isSelected = card.labels.selectedLabelIds.includes(labelId);
    const updated = isSelected
      ? card.labels.selectedLabelIds.filter((id) => id !== labelId)
      : [...card.labels.selectedLabelIds, labelId];

    setCard((prev) =>
      prev
        ? { ...prev, labels: { ...prev.labels, selectedLabelIds: updated } }
        : null
    );

    try {
      await toggleCardLabel({
        cardId: Number(cardId),
        labelId: Number(labelId),
        selected: !isSelected,
      });
      loadActivities();
    } catch (err) {
      console.error('Toggle label failed', err);
      setCard((prev) =>
        prev
          ? {
              ...prev,
              labels: {
                ...prev.labels,
                selectedLabelIds: isSelected
                  ? [...prev.labels.selectedLabelIds, labelId]
                  : prev.labels.selectedLabelIds.filter((id) => id !== labelId),
              },
            }
          : null
      );
    }
  };

  // Date save
  const handleSaveDate = async (date: string | null, reminder: boolean) => {
    await updateCard(cardId, { dueDate: date, dueReminder: reminder });
    setCard((prev) =>
      prev ? { ...prev, dueDate: date, dueReminder: reminder } : null
    );
  };

  // Toggle checklist item
  const handleToggleChecklistItem = async (clId: string, itemId: string) => {
    const isCompleted = card?.checklists
      .find((cl) => cl.id === clId)
      ?.items.find((i) => i.id === itemId)?.isCompleted;

    setCard((prev) => {
      if (!prev) return null;
      return {
        ...prev,
        checklists: prev.checklists.map((cl) =>
          cl.id === clId
            ? {
                ...cl,
                items: cl.items.map((item) =>
                  item.id === itemId
                    ? { ...item, isCompleted: !item.isCompleted }
                    : item
                ),
              }
            : cl
        ),
      };
    });

    try {
      await toggleChecklistItem(Number(itemId));
      loadActivities();
    } catch (err) {
      console.error('Toggle checklist item failed', err);
      setCard((prev) => {
        if (!prev) return null;
        return {
          ...prev,
          checklists: prev.checklists.map((cl) =>
            cl.id === clId
              ? {
                  ...cl,
                  items: cl.items.map((item) =>
                    item.id === itemId
                      ? { ...item, isCompleted: isCompleted ?? false }
                      : item
                  ),
                }
              : cl
          ),
        };
      });
    }
  };

  // ===== Yêu cầu 1: xóa checklist item =====
  const handleDeleteChecklistItem = async (clId: string, itemId: string) => {
    // snapshot để rollback
    const snapshot = card?.checklists ?? [];

    // optimistic UI
    setCard((prev) => {
      if (!prev) return null;
      return {
        ...prev,
        checklists: prev.checklists.map((cl) =>
          cl.id === clId
            ? { ...cl, items: cl.items.filter((i) => i.id !== itemId) }
            : cl
        ),
      };
    });

    try {
      await deleteChecklistItem(Number(itemId));
      loadActivities();
    } catch (err) {
      console.error('Delete checklist item failed', err);
      // rollback
      setCard((prev) => (prev ? { ...prev, checklists: snapshot } : null));
    }
  };

  // Add checklist item inline
  const handleAddChecklistItem = async (clId: string, content: string) => {
    try {
      const res = await createChecklistItem(Number(clId), {
        content,
        position: Date.now(),
        dueDate: null,
        afterId: null,
        beforeId: null,
      });

      const newItem: ChecklistItem = {
        id: String(res.id),
        content: res.content,
        isCompleted: res.isDone,
      };

      setCard((prev) => {
        if (!prev) return null;
        return {
          ...prev,
          checklists: prev.checklists.map((cl) =>
            cl.id === clId ? { ...cl, items: [...cl.items, newItem] } : cl
          ),
        };
      });
    } catch (err) {
      console.error('Create checklist item error:', err);
    }
  };

  // ===== Yêu cầu 2: xóa checklist — gọi API =====
  const handleDeleteChecklist = async (clId: string) => {
    const snapshot = card?.checklists ?? [];

    // optimistic UI
    setCard((prev) =>
      prev
        ? {
            ...prev,
            checklists: prev.checklists.filter((cl) => cl.id !== clId),
          }
        : null
    );

    try {
      await deleteChecklist(Number(clId));
      loadActivities(); // Yêu cầu 5
    } catch (err) {
      console.error('Delete checklist failed', err);
      // rollback
      setCard((prev) => (prev ? { ...prev, checklists: snapshot } : null));
    }
  };

  // Drag end for checklist items
  const handleChecklistItemDragEnd = (clId: string, event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    setCard((prev) => {
      if (!prev) return null;
      return {
        ...prev,
        checklists: prev.checklists.map((cl) => {
          if (cl.id !== clId) return cl;
          const oldIdx = cl.items.findIndex((i) => i.id === String(active.id));
          const newIdx = cl.items.findIndex((i) => i.id === String(over.id));
          if (oldIdx === -1 || newIdx === -1) return cl;
          return { ...cl, items: arrayMove(cl.items, oldIdx, newIdx) };
        }),
      };
    });
  };

  // Attachment
  const handleAttach = async (file: File) => {
    try {
      const res = await uploadAttachment(Number(cardId), file);
      const newAttachment: Attachment = {
        id: String(res.id),
        fileName: res.fileName,
        fileUrl: res.fileUrl,
        fileSize: res.fileSize,
        mimeType: res.mimeType,
        isCover: res.isCover,
      };
      setCard((prev) =>
        prev
          ? { ...prev, attachments: [...prev.attachments, newAttachment] }
          : null
      );
      loadActivities();
    } catch (err) {
      console.error('Upload attachment error:', err);
    }
  };

  const handleDeleteAttachment = async (id: string) => {
    const oldAttachments = card?.attachments || [];
    setCard((prev) =>
      prev
        ? { ...prev, attachments: prev.attachments.filter((a) => a.id !== id) }
        : null
    );
    try {
      await deleteAttachment(Number(id));
      loadActivities();
    } catch (err) {
      console.error('Delete attachment failed', err);
      setCard((prev) =>
        prev ? { ...prev, attachments: oldAttachments } : null
      );
    }
  };

  // ===== Yêu cầu 4: xóa card =====
  const handleDeleteCard = async () => {
    setDeletingCard(true);
    try {
      await archiveCard(Number(cardId));
      onCardDeleted?.(cardId);
      onClose();
    } catch (err) {
      console.error('Delete card failed', err);
      setDeletingCard(false);
    }
  };

  // Comment submit
  const handleCommentSubmit = async () => {
    if (!commentInput.trim() || submittingComment) return;
    setSubmittingComment(true);
    try {
      const newComment = await createComment(Number(cardId), {
        content: commentInput.trim(),
      });
      setComments((prev) => [...prev, newComment]);
      setCommentInput('');
    } catch (err) {
      console.error('Lỗi gửi bình luận:', err);
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleCommentKeyDown = (
    e: React.KeyboardEvent<HTMLTextAreaElement>
  ) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleCommentSubmit();
    }
  };

  // ===== Render =====
  if (loading || !card) return null;

  const {
    title,
    description,
    isCompleted,
    dueDate,
    dueReminder,
    labels,
    checklists,
    attachments,
  } = card;

  const checklistProgress = (cl: Checklist) => {
    if (cl.items.length === 0) return 0;
    return Math.round(
      (cl.items.filter((i) => i.isCompleted).length / cl.items.length) * 100
    );
  };

  const formatFileSize = (bytes?: number) => {
    if (!bytes) return '';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const renderActiveModal = () => {
    switch (activeModal) {
      case 'addMenu':
        return (
          <AddMenuModal
            onSelect={(type) => setActiveModal(type)}
            onClose={closeModal}
          />
        );
      case 'label':
        return (
          <LabelModal
            boardLabels={labels.boardLabels}
            selectedIds={labels.selectedLabelIds}
            onToggle={handleToggleLabel}
            onClose={closeModal}
            boardSlug={boardSlug!}
            onAddLabel={(newLabel) =>
              setCard((prev) =>
                prev
                  ? {
                      ...prev,
                      labels: {
                        ...prev.labels,
                        boardLabels: [...prev.labels.boardLabels, newLabel],
                      },
                    }
                  : null
              )
            }
          />
        );
      case 'date':
        return (
          <DateModal
            dueDate={dueDate}
            dueReminder={dueReminder}
            onSave={handleSaveDate}
            onClose={closeModal}
          />
        );
      case 'checklist':
        return (
          <ChecklistModal
            cardId={Number(cardId)}
            onAdd={(newChecklist) =>
              setCard((prev) =>
                prev
                  ? { ...prev, checklists: [...prev.checklists, newChecklist] }
                  : null
              )
            }
            onCreated={loadActivities}
            onClose={closeModal}
          />
        );
      case 'attachment':
        return <AttachmentModal onAttach={handleAttach} onClose={closeModal} />;
      default:
        return null;
    }
  };

  return (
    <div className="cdm-backdrop" onClick={handleClose}>
      <div className="cdm-modal" onClick={(e) => e.stopPropagation()}>
        {/* HEADER */}
        <div className="cdm-header">
          <div className="cdm-header-top">
            <div className="cdm-header-actions">
              <button
                className="cdm-icon-btn cdm-close-btn"
                onClick={handleClose}
                title="Đóng"
              >
                ✕
              </button>
            </div>
          </div>

          <div className="cdm-title-row">
            <button
              className={`cdm-complete-btn ${isCompleted ? 'is-done' : ''}`}
              onClick={handleToggleComplete}
              title="Đánh dấu hoàn tất"
            >
              ✓
            </button>
            <h2
              className={`cdm-card-title ${isCompleted ? 'is-completed' : ''}`}
              contentEditable
              suppressContentEditableWarning
              onBlur={handleUpdateTitle}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  (e.target as HTMLElement).blur();
                }
              }}
            >
              {title}
            </h2>
          </div>

          <div className="cdm-header-meta">
            <span style={{ fontSize: '13px', color: '#9fadbc' }}>
              Nằm trong danh sách <u>NextStep Board</u>
            </span>
          </div>
        </div>

        <div className="cdm-body">
          {/* LEFT */}
          <div className="cdm-left">
            {/* Action bar */}
            <div className="cdm-actions-row">
              {labels.selectedLabelIds.length === 0 && (
                <button
                  className="cdm-action-btn cdm-action-btn--add"
                  onClick={() => openModal('label')}
                >
                  + Nhãn
                </button>
              )}
              {!dueDate && (
                <button
                  className="cdm-action-btn cdm-action-btn--add"
                  onClick={() => openModal('date')}
                >
                  + Ngày
                </button>
              )}
              <button
                className="cdm-action-btn cdm-action-btn--more"
                onClick={() => openModal('addMenu')}
              >
                + Thêm
              </button>
            </div>

            {/* LABELS & DUE DATE meta */}
            <div
              className="cdm-header-meta"
              style={{ paddingLeft: 0, marginBottom: '20px' }}
            >
              {labels.selectedLabelIds.length > 0 && (
                <div
                  className="cdm-meta-labels"
                  onClick={() => openModal('label')}
                  title="Chỉnh sửa nhãn"
                >
                  {labels.boardLabels
                    .filter((l) => labels.selectedLabelIds.includes(l.id))
                    .map((l) => (
                      <span
                        key={l.id}
                        className="cdm-label"
                        style={{ background: l.color }}
                      >
                        {l.name}
                      </span>
                    ))}
                  <span className="cdm-label cdm-label--edit">✏</span>
                </div>
              )}

              {dueDate && (
                <div className="cdm-meta-due">
                  <div
                    className={`cdm-due-badge ${isCompleted ? 'done' : ''}`}
                    onClick={() => openModal('date')}
                    title="Chỉnh sửa ngày"
                  >
                    <span>🕐</span>
                    {new Date(dueDate).toLocaleString('vi-VN')}
                  </div>
                  <label className="cdm-reminder-check">
                    <input
                      type="checkbox"
                      checked={dueReminder}
                      onChange={async () => {
                        const newVal = !dueReminder;
                        await updateCard(cardId, { dueReminder: newVal });
                        setCard((prev) =>
                          prev ? { ...prev, dueReminder: newVal } : null
                        );
                      }}
                    />
                    Nhắc nhở
                  </label>
                </div>
              )}
            </div>

            {/* MÔ TẢ */}
            <div className="cdm-section">
              <h4 className="cdm-section-label">Mô tả</h4>
              <textarea
                className="cdm-description"
                defaultValue={description}
                placeholder="Thêm mô tả chi tiết..."
                onBlur={handleUpdateDescription}
              />
            </div>

            {/* CHECKLISTS */}
            {checklists.map((cl) => {
              const pct = checklistProgress(cl);
              return (
                <div key={cl.id} className="cdm-checklist">
                  <div className="cdm-checklist-header">
                    <span className="cdm-checklist-title">☑ {cl.title}</span>
                    <div
                      style={{
                        display: 'flex',
                        gap: '6px',
                        alignItems: 'center',
                      }}
                    >
                      <span className="cdm-checklist-pct">{pct}%</span>
                      {/* Yêu cầu 2: nút Xóa checklist giờ gọi API */}
                      <button
                        className="cdm-action-btn"
                        onClick={() => handleDeleteChecklist(cl.id)}
                      >
                        Xóa
                      </button>
                    </div>
                  </div>

                  <div className="cdm-progress-bar">
                    <div
                      className="cdm-progress-fill"
                      style={{
                        width: `${pct}%`,
                        background: pct === 100 ? '#4ade80' : '#579dff',
                      }}
                    />
                  </div>

                  <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    onDragEnd={(e) => handleChecklistItemDragEnd(cl.id, e)}
                  >
                    <SortableContext
                      items={cl.items.map((i) => i.id)}
                      strategy={verticalListSortingStrategy}
                    >
                      <div className="cdm-checklist-items">
                        {cl.items.map((item) => (
                          <SortableChecklistItem
                            key={item.id}
                            item={item}
                            onToggle={(itemId) =>
                              handleToggleChecklistItem(cl.id, itemId)
                            }
                            /* Yêu cầu 1: truyền handler xóa item */
                            onDelete={(itemId) =>
                              handleDeleteChecklistItem(cl.id, itemId)
                            }
                          />
                        ))}
                      </div>
                    </SortableContext>
                  </DndContext>

                  <AddChecklistItemInline
                    onAdd={(content) => handleAddChecklistItem(cl.id, content)}
                  />
                </div>
              );
            })}

            {/* ĐÍNH KÈM */}
            {attachments.length > 0 && (
              <div className="cdm-section">
                <h4 className="cdm-section-label">Tệp đính kèm</h4>
                <div className="cdm-attachments">
                  {attachments.map((a) => (
                    <div key={a.id} className="cdm-attachment">
                      <div className="cdm-attachment-thumb">
                        {a.mimeType?.startsWith('image/') ? (
                          <img src={a.fileUrl} alt={a.fileName} />
                        ) : (
                          <span className="cdm-attachment-icon">📄</span>
                        )}
                      </div>
                      <div className="cdm-attachment-info">
                        <a
                          href={a.fileUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="cdm-attachment-name"
                        >
                          {a.fileName}
                        </a>
                        <span className="cdm-attachment-date">
                          {formatFileSize(a.fileSize)}
                          {a.isCover && (
                            <span
                              className="cdm-cover-badge"
                              style={{ marginLeft: '6px' }}
                            >
                              Ảnh bìa
                            </span>
                          )}{' '}
                          •{' '}
                          <u
                            style={{ cursor: 'pointer' }}
                            onClick={() => handleDeleteAttachment(a.id)}
                          >
                            Xóa
                          </u>
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
                <button
                  className="cdm-add-inline-btn"
                  style={{ marginTop: '8px' }}
                  onClick={() => openModal('attachment')}
                >
                  + Đính kèm thêm
                </button>
              </div>
            )}

            {/* BÌNH LUẬN & HOẠT ĐỘNG */}
            <div className="cdm-section">
              <h4 className="cdm-section-label">Hoạt động</h4>
              <div className="cdm-comment-input-row">
                <div className="cdm-comment-input-wrap">
                  <textarea
                    className="cdm-comment-input"
                    placeholder="Viết bình luận... (Enter để gửi, Shift+Enter xuống dòng)"
                    value={commentInput}
                    onChange={(e) => setCommentInput(e.target.value)}
                    onKeyDown={handleCommentKeyDown}
                    rows={commentInput ? 3 : 1}
                  />
                  {commentInput.trim() && (
                    <div className="cdm-comment-actions">
                      <button
                        className="cdm-comment-submit"
                        onClick={handleCommentSubmit}
                        disabled={submittingComment}
                      >
                        {submittingComment ? 'Đang gửi...' : 'Lưu'}
                      </button>
                      <button
                        className="cdm-comment-cancel"
                        onClick={() => setCommentInput('')}
                      >
                        Hủy
                      </button>
                    </div>
                  )}
                </div>
              </div>

              <div className="cdm-activity-list">
                {comments.map((c) => (
                  <div key={c.id} className="cdm-activity-item">
                    <div
                      className="cdm-avatar"
                      style={{ background: '#8590a2', overflow: 'hidden' }}
                    >
                      {c.avatarUrl ? (
                        <img
                          src={c.avatarUrl}
                          alt={c.userName}
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover',
                          }}
                        />
                      ) : (
                        c.userName?.charAt(0).toUpperCase()
                      )}
                    </div>
                    <div className="cdm-activity-content">
                      <span className="cdm-activity-author">{c.userName}</span>
                      <div className="cdm-comment-bubble">
                        <p className="cdm-comment-text">{c.content}</p>
                      </div>
                    </div>
                  </div>
                ))}
                {commentHasMore && (
                  <button
                    className="cdm-load-more"
                    onClick={() => loadComments(commentPage + 1)}
                  >
                    Xem thêm bình luận
                  </button>
                )}

                {activities.map((a) => (
                  <div key={a.id} className="cdm-activity-item">
                    <div
                      className="cdm-avatar cdm-avatar--sm"
                      style={{ background: 'rgba(255,255,255,0.1)' }}
                    >
                      •
                    </div>
                    <div className="cdm-activity-content">
                      <span className="cdm-activity-text">{a.message}</span>
                      <span className="cdm-activity-time">Vừa xong</span>
                    </div>
                  </div>
                ))}
                {activityHasMore && (
                  <button
                    className="cdm-load-more"
                    onClick={() => loadActivities(activityPage + 1)}
                  >
                    Xem thêm hoạt động
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* RIGHT SIDEBAR */}
          <div className="cdm-right">
            <h4 className="cdm-sidebar-label">Thêm vào thẻ</h4>
            <button
              className="cdm-sidebar-btn"
              onClick={() => openModal('label')}
            >
              🏷 Nhãn
            </button>
            <button
              className="cdm-sidebar-btn"
              onClick={() => openModal('checklist')}
            >
              ☑ Checklist
            </button>
            <button
              className="cdm-sidebar-btn"
              onClick={() => openModal('date')}
            >
              🕐 Ngày
            </button>
            <button
              className="cdm-sidebar-btn"
              onClick={() => openModal('attachment')}
            >
              📎 Đính kèm
            </button>

            <h4 className="cdm-sidebar-label" style={{ marginTop: '24px' }}>
              Thao tác
            </h4>
            <button className="cdm-sidebar-btn">→ Di chuyển</button>
            <button className="cdm-sidebar-btn">❐ Sao chép</button>
            <div className="cdm-sidebar-divider"></div>

            <button
              className="cdm-sidebar-btn cdm-sidebar-btn--danger"
              onClick={() => setShowDeleteModal(true)}
              disabled={deletingCard}
            >
              {deletingCard ? '⏳ Đang xóa...' : '☒ Lưu trữ'}
            </button>

            {showDeleteModal && (
              <div className="archive-card-modal-overlay">
                <div className="archive-card-modal">
                  <h3>Bạn có chắc?</h3>
                  <p>Thẻ này sẽ được lưu trữ.</p>

                  <div className="archive-card-modal-actions">
                    <button
                      className="cdm-btn"
                      onClick={() => setShowDeleteModal(false)}
                      disabled={deletingCard}
                    >
                      Hủy
                    </button>

                    <button
                      className="cdm-btn cdm-btn--danger"
                      onClick={handleDeleteCard}
                      disabled={deletingCard}
                    >
                      {deletingCard ? '⏳ Đang xử lý...' : 'Xác nhận'}
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* MODALS */}
        {renderActiveModal()}
      </div>
    </div>
  );
}

// ============================================================
// 10. ADD CHECKLIST ITEM INLINE
// ============================================================
function AddChecklistItemInline({
  onAdd,
}: {
  onAdd: (content: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const [value, setValue] = useState('');

  if (!open) {
    return (
      <button
        className="cdm-add-inline-btn"
        style={{ marginTop: '8px' }}
        onClick={() => setOpen(true)}
      >
        + Thêm mục
      </button>
    );
  }

  return (
    <div
      style={{
        marginTop: '8px',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
      }}
    >
      <input
        className="cdm-inner-input"
        placeholder="Nội dung mục..."
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && value.trim()) {
            onAdd(value.trim());
            setValue('');
          }
          if (e.key === 'Escape') setOpen(false);
        }}
        autoFocus
      />
      <div style={{ display: 'flex', gap: '6px' }}>
        <button
          className="cdm-comment-submit"
          onClick={() => {
            if (value.trim()) {
              onAdd(value.trim());
              setValue('');
            }
          }}
        >
          Thêm
        </button>
        <button className="cdm-comment-cancel" onClick={() => setOpen(false)}>
          Hủy
        </button>
      </div>
    </div>
  );
}
