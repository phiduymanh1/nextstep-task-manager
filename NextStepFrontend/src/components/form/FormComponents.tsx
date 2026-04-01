import { useState, useRef } from 'react';

// ===== AddListCard =====
interface AddListCardProps {
  onAdd: (name: string) => Promise<void>;
}

export function AddListCard({ onAdd }: AddListCardProps) {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const openForm = () => {
    setOpen(true);
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  const close = () => {
    setOpen(false);
    setName('');
  };

  const submit = async () => {
    const trimmed = name.trim();
    if (!trimmed || isSubmitting) return;
    setIsSubmitting(true);
    try {
      await onAdd(trimmed);
      close();
    } catch {
      // keep form open on error
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!open) {
    return (
      <button className="add-list-btn" onClick={openForm}>
        <span className="add-list-plus">＋</span>
        Thêm danh sách khác
      </button>
    );
  }

  return (
    <div className="add-list-form">
      <input
        ref={inputRef}
        className="add-list-input"
        placeholder="Nhập tên danh sách..."
        value={name}
        maxLength={100}
        onChange={(e) => setName(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter') submit();
          if (e.key === 'Escape') close();
        }}
        disabled={isSubmitting}
      />
      <div className="add-list-actions">
        <button
          className="add-list-confirm"
          onClick={submit}
          disabled={!name.trim() || isSubmitting}
        >
          {isSubmitting ? '...' : 'Thêm danh sách'}
        </button>
        <button
          className="add-list-cancel"
          onClick={close}
          disabled={isSubmitting}
        >
          ✕
        </button>
      </div>
    </div>
  );
}

// ===== AddCardForm =====
interface AddCardFormProps {
  onAdd: (title: string) => Promise<void>;
  onCancel: () => void;
}

export function AddCardForm({ onAdd, onCancel }: AddCardFormProps) {
  const [title, setTitle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  // Auto-focus on mount
  const refCallback = (el: HTMLTextAreaElement | null) => {
    if (el) {
      (inputRef as React.MutableRefObject<HTMLTextAreaElement | null>).current =
        el;
      el.focus();
    }
  };

  const submit = async () => {
    const trimmed = title.trim();
    if (!trimmed || isSubmitting) return;

    if (trimmed.length > 255) {
      setError('Tiêu đề tối đa 255 ký tự');
      return;
    }

    setError(null);
    setIsSubmitting(true);
    try {
      await onAdd(trimmed);
      setTitle('');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Không thể tạo thẻ, thử lại sau');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="add-card-form">
      <textarea
        ref={refCallback}
        className={`add-card-input${error ? ' add-card-input--error' : ''}`}
        placeholder="Nhập tiêu đề thẻ..."
        value={title}
        rows={2}
        maxLength={255}
        onChange={(e) => {
          setTitle(e.target.value);
          if (error) setError(null);
        }}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            submit();
          }
          if (e.key === 'Escape') onCancel();
        }}
        disabled={isSubmitting}
      />
      {error && <div className="add-card-error">{error}</div>}
      <div className="add-card-actions">
        <button
          className="add-card-confirm"
          onClick={submit}
          disabled={!title.trim() || isSubmitting}
        >
          {isSubmitting ? <span className="add-card-spinner" /> : 'Thêm thẻ'}
        </button>
        <button
          className="add-card-cancel"
          onClick={onCancel}
          disabled={isSubmitting}
          title="Huỷ (Esc)"
        >
          ✕
        </button>
      </div>
    </div>
  );
}
