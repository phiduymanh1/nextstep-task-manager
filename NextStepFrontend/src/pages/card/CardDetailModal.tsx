import React, { useEffect, useState } from 'react';
import '@/assets/styles/CardDetailModal.css';
import { getCardDetail, updateCard } from '@/services/card.service';
import { getComments } from '@/services/comment.service';
import { getActivities } from '@/services/activity.service';

// ============================================================
// 1. TYPES DEFINITION
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
  items: ChecklistItem[];
}

interface Attachment {
  id: string;
  fileName: string;
  fileUrl: string;
}

interface Comment {
  id: string;
  userId: string;
  content: string;
}

interface Activity {
  id: string;
  message: string;
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
}

// ============================================================
// 2. MAIN COMPONENT
// ============================================================
export default function CardDetailModal({
  cardId,
  onClose,
  onToggleComplete,
}: Props) {
  const [loading, setLoading] = useState(true);
  const [card, setCard] = useState<CardData | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);

  // ===== Load dữ liệu ban đầu =====
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [detail, cmt, act] = await Promise.all([
          getCardDetail(cardId),
          getComments(cardId),
          getActivities(cardId),
        ]);
        setCard(detail);
        setComments(cmt);
        setActivities(act);
      } catch (error) {
        console.error('Lỗi load card:', error);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [cardId]);

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

  // ===== Xử lý cập nhật Tiêu đề =====
  const handleUpdateTitle = async (e: React.FocusEvent<HTMLHeadingElement>) => {
    const newTitle = e.currentTarget.textContent?.trim();
    if (newTitle && newTitle !== title) {
      await updateCard(cardId, { title: newTitle });
      setCard((prev) => (prev ? { ...prev, title: newTitle } : null));
    }
  };

  // ===== Xử lý cập nhật Mô tả =====
  const handleUpdateDescription = async (
    e: React.FocusEvent<HTMLTextAreaElement>
  ) => {
    const newDesc = e.target.value;
    if (newDesc !== description) {
      await updateCard(cardId, { description: newDesc });
      setCard((prev) => (prev ? { ...prev, description: newDesc } : null));
    }
  };

  // ===== Toggle hoàn thành thẻ =====
  const handleToggleComplete = async () => {
    const newVal = !isCompleted;
    await updateCard(cardId, { isCompleted: newVal });
    setCard((prev) => (prev ? { ...prev, isCompleted: newVal } : null));
    onToggleComplete(cardId, newVal);
  };

  // ===== Toggle nhắc nhở =====
  const handleToggleReminder = async () => {
    const newVal = !dueReminder;
    await updateCard(cardId, { dueReminder: newVal });
    setCard((prev) => (prev ? { ...prev, dueReminder: newVal } : null));
  };

  const quickActions: string[] = [];
  if (!dueDate) quickActions.push('Ngày');
  if (!labels?.selectedLabelIds?.length) quickActions.push('Nhãn');

  return (
    <div className="cdm-backdrop" onClick={onClose}>
      <div className="cdm-modal" onClick={(e) => e.stopPropagation()}>
        {/* HEADER SECTION */}
        <div className="cdm-header">
          <div className="cdm-header-top">
            <div className="cdm-header-actions">
              <button
                className="cdm-icon-btn cdm-close-btn"
                onClick={onClose}
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
          {/* CỘT TRÁI - NỘI DUNG CHI TIẾT */}
          <div className="cdm-left">
            {/* Quick Actions Bar */}
            {quickActions.length > 0 && (
              <div className="cdm-actions-row">
                {quickActions.map((action) => (
                  <button
                    key={action}
                    className="cdm-action-btn cdm-action-btn--add"
                  >
                    + {action}
                  </button>
                ))}
              </div>
            )}

            {/* NHÃN & NGÀY (Metadata) */}
            <div
              className="cdm-header-meta"
              style={{ paddingLeft: 0, marginBottom: '20px' }}
            >
              {labels.selectedLabelIds.length > 0 && (
                <div className="cdm-meta-labels">
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
                </div>
              )}

              {dueDate && (
                <div className="cdm-meta-due">
                  <div className={`cdm-due-badge ${isCompleted ? 'done' : ''}`}>
                    <span style={{ marginRight: '8px' }}>🕐</span>
                    {new Date(dueDate).toLocaleString('vi-VN')}
                  </div>
                  <label className="cdm-reminder-check">
                    <input
                      type="checkbox"
                      checked={dueReminder}
                      onChange={handleToggleReminder}
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
                placeholder="Thêm mô tả chi tiết cho công việc này..."
                onBlur={handleUpdateDescription}
              />
            </div>

            {/* CHECKLISTS */}
            {checklists.map((cl) => (
              <div key={cl.id} className="cdm-checklist">
                <div className="cdm-checklist-header">
                  <span className="cdm-checklist-title">☑ {cl.title}</span>
                  <button className="cdm-action-btn">Xóa</button>
                </div>

                {/* Progress bar giả định 50% hoặc tính toán từ item */}
                <div className="cdm-progress-bar">
                  <div
                    className="cdm-progress-fill"
                    style={{ width: '50%' }}
                  ></div>
                </div>

                <div className="cdm-checklist-items">
                  {cl.items.map((item) => (
                    <label key={item.id} className="cdm-checklist-item">
                      <input
                        type="checkbox"
                        checked={item.isCompleted}
                        readOnly
                      />
                      <span className={item.isCompleted ? 'done' : ''}>
                        {item.content}
                      </span>
                    </label>
                  ))}
                </div>
                <button
                  className="cdm-add-inline-btn"
                  style={{ marginTop: '8px' }}
                >
                  + Thêm mục mới
                </button>
              </div>
            ))}

            {/* ĐÍNH KÈM */}
            <div className="cdm-section">
              <h4 className="cdm-section-label">Tệp đính kèm</h4>
              <div className="cdm-attachments">
                {attachments.map((a) => (
                  <div key={a.id} className="cdm-attachment">
                    <div className="cdm-attachment-thumb">
                      <span className="cdm-attachment-icon">📄</span>
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
                        Đã thêm vào hệ thống • <u>Xóa</u>
                      </span>
                    </div>
                  </div>
                ))}
              </div>
              <button
                className="cdm-add-inline-btn"
                style={{ marginTop: '8px' }}
              >
                + Đính kèm tệp
              </button>
            </div>

            {/* BÌNH LUẬN & HOẠT ĐỘNG */}
            <div className="cdm-section">
              <h4 className="cdm-section-label">Hoạt động</h4>
              <div className="cdm-comment-input-row">
                <div className="cdm-avatar" style={{ background: '#579dff' }}>
                  M
                </div>
                <div className="cdm-comment-input-wrap">
                  <textarea
                    className="cdm-comment-input"
                    placeholder="Viết bình luận..."
                    rows={1}
                  />
                </div>
              </div>

              <div className="cdm-activity-list">
                {comments.map((c) => (
                  <div key={c.id} className="cdm-activity-item">
                    <div
                      className="cdm-avatar"
                      style={{ background: '#8590a2' }}
                    >
                      U
                    </div>
                    <div className="cdm-activity-content">
                      <span className="cdm-activity-author">
                        User {c.userId}
                      </span>
                      <div className="cdm-comment-bubble">
                        <p className="cdm-comment-text">{c.content}</p>
                      </div>
                    </div>
                  </div>
                ))}

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
              </div>
            </div>
          </div>

          {/* CỘT PHẢI - SIDEBAR ACTIONS */}
          <div className="cdm-right">
            <h4 className="cdm-sidebar-label">Thêm vào thẻ</h4>
            <button className="cdm-sidebar-btn">🏷 Nhãn</button>
            <button className="cdm-sidebar-btn">☑ Checklist</button>
            <button className="cdm-sidebar-btn">🕐 Ngày</button>
            <button className="cdm-sidebar-btn">📎 Đính kèm</button>

            <h4 className="cdm-sidebar-label" style={{ marginTop: '24px' }}>
              Thao tác
            </h4>
            <button className="cdm-sidebar-btn">→ Di chuyển</button>
            <button className="cdm-sidebar-btn">❐ Sao chép</button>
            <div className="cdm-sidebar-divider"></div>
            <button className="cdm-sidebar-btn cdm-sidebar-btn--danger">
              ☒ Lưu trữ
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
