import { useState, useEffect } from 'react';
import { ZodError } from 'zod';
import toast from 'react-hot-toast';
import { createBoard } from '@/services/board.service';
import { boardSchema } from '@/schema/board.schema';
import type { BoardSchema } from '@/schema/board.schema';
import '@/assets/styles/CreateBoardModal.css';

type Props = {
  open: boolean;
  onClose: () => void;
  workspaceSlug: string;
  onSuccess: () => Promise<void>;
};

const BG_COLORS = [
  '#2c7be5',
  '#00b4d8',
  '#06d6a0',
  '#f4a261',
  '#e63946',
  '#7b2d8b',
  '#2c2c2c',
  '#4a4e69',
];

type Visibility = BoardSchema['visibility'];

const VISIBILITY_OPTIONS: { value: Visibility; icon: string; label: string }[] =
  [
    { value: 'PRIVATE', icon: '🔒', label: 'Private' },
    { value: 'WORKSPACE', icon: '👥', label: 'Workspace' },
    { value: 'PUBLIC', icon: '🌐', label: 'Public' },
  ];

// ✅ initial form tách riêng
const initialForm = {
  name: '',
  description: '',
  backgroundColor: BG_COLORS[0],
  visibility: 'PRIVATE' as Visibility,
};

export default function CreateBoardModal({
  open,
  onClose,
  workspaceSlug,
  onSuccess,
}: Props) {
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // reset khi mở modal
  useEffect(() => {
    if (open) {
      setForm(initialForm);
      setError('');
    }
  }, [open]);

  if (!open) return null;

  const handleClose = () => {
    setForm(initialForm);
    setError('');
    onClose();
  };

  const handleCreate = async () => {
    try {
      setError('');
      setLoading(true);

      const parsed = boardSchema.parse(form);

      await createBoard(workspaceSlug, parsed);
      await onSuccess();
      handleClose();
    } catch (err) {
      if (err instanceof ZodError) {
        setError(err.issues[0].message);
      } else {
        toast.error('Tạo bảng thất bại');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Tạo bảng mới</h3>

        {/* Preview */}
        <div
          className="board-preview"
          style={{ backgroundColor: form.backgroundColor }}
        >
          <span className="board-preview-name">{form.name || 'Tên bảng'}</span>
        </div>

        {/* Color picker */}
        <div className="color-picker">
          {BG_COLORS.map((color) => (
            <button
              key={color}
              type="button"
              className={`color-swatch ${
                form.backgroundColor === color ? 'active' : ''
              }`}
              style={{ backgroundColor: color }}
              onClick={() => {
                setForm((prev) => ({ ...prev, backgroundColor: color }));
                setError('');
              }}
            />
          ))}
        </div>

        {/* Name */}
        <input
          placeholder="Tên bảng"
          value={form.name}
          onChange={(e) => {
            setForm((prev) => ({ ...prev, name: e.target.value }));
            setError('');
          }}
          onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
          autoFocus
        />

        {/* Description */}
        <textarea
          placeholder="Mô tả (tuỳ chọn)"
          value={form.description}
          onChange={(e) => {
            setForm((prev) => ({ ...prev, description: e.target.value }));
            setError('');
          }}
        />

        {/* Visibility */}
        <div className="visibility-group">
          {VISIBILITY_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              type="button"
              className={`visibility-btn ${
                form.visibility === opt.value ? 'active' : ''
              }`}
              onClick={() => {
                setForm((prev) => ({ ...prev, visibility: opt.value }));
                setError('');
              }}
            >
              {opt.icon} {opt.label}
            </button>
          ))}
        </div>

        {/* Error */}
        {error && <p className="error">{error}</p>}

        {/* Actions */}
        <div className="actions">
          <button className="primary" onClick={handleCreate} disabled={loading}>
            {loading ? 'Đang tạo...' : 'Tạo bảng'}
          </button>

          <button
            className="secondary"
            onClick={handleClose}
            disabled={loading}
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
