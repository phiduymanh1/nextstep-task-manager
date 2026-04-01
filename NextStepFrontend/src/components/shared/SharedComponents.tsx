import { useOnClickOutside } from '@/hook/Hooks';
import { VISIBILITY_OPTIONS } from '@/types/card.type';
import type { Visibility } from '@/types/workspace.type';
import { useState, useRef } from 'react';

// ===== InlineEdit =====
interface InlineEditProps {
  value: string;
  onSave: (v: string) => void;
  className?: string;
  inputClassName?: string;
  multiline?: boolean;
  placeholder?: string;
}

export function InlineEdit({
  value,
  onSave,
  className,
  inputClassName,
  multiline = false,
  placeholder = 'Nhấp đôi để thêm...',
}: InlineEditProps) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value);
  const ref = useRef<HTMLInputElement & HTMLTextAreaElement>(null);

  const start = () => {
    setDraft(value);
    setEditing(true);
    requestAnimationFrame(() => ref.current?.select());
  };

  const commit = () => {
    const v = draft.trim();
    if (!v) {
      setDraft(value);
    } else if (v !== value) {
      onSave(v);
    }
    setEditing(false);
  };

  const cancel = () => {
    setDraft(value);
    setEditing(false);
  };

  if (editing) {
    const props = {
      ref,
      className: inputClassName ?? 'inline-edit-input',
      value: draft,
      onChange: (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
      ) => setDraft(e.target.value),
      onBlur: commit,
      onKeyDown: (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && !multiline) commit();
        if (e.key === 'Escape') cancel();
      },
    };
    return multiline ? <textarea {...props} rows={3} /> : <input {...props} />;
  }

  return (
    <span className={className} onDoubleClick={start}>
      {value || <em style={{ opacity: 0.4 }}>{placeholder}</em>}
    </span>
  );
}

// ===== VisibilityDropdown =====
interface VisibilityDropdownProps {
  value: Visibility;
  onChange: (v: Visibility) => void;
}

export function VisibilityDropdown({
  value,
  onChange,
}: VisibilityDropdownProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useOnClickOutside(ref as React.RefObject<HTMLElement>, () => setOpen(false));
  const current = VISIBILITY_OPTIONS.find((o) => o.value === value)!;

  return (
    <div className="visibility-dropdown" ref={ref}>
      <button className="visibility-btn" onClick={() => setOpen((p) => !p)}>
        <span>{current.icon}</span>
        <span>{current.label}</span>
        <span className={`visibility-chevron${open ? ' open' : ''}`}>▾</span>
      </button>
      {open && (
        <div className="visibility-menu">
          <div className="visibility-menu-title">Hiển thị bảng</div>
          {VISIBILITY_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              className={`visibility-item${value === opt.value ? ' active' : ''}`}
              onClick={() => {
                onChange(opt.value);
                setOpen(false);
              }}
            >
              <div className="visibility-item-header">
                <span>
                  {opt.icon} {opt.label}
                </span>
                {value === opt.value && (
                  <span className="visibility-check">✓</span>
                )}
              </div>
              <div className="visibility-item-desc">{opt.desc}</div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// ===== BoardSkeleton =====
export function BoardSkeleton() {
  return (
    <div className="board-columns">
      {[1, 2, 3].map((i) => (
        <div key={i} className="board-column board-column--skeleton">
          <div className="skeleton skeleton--title" />
          {[1, 2, 3].map((j) => (
            <div key={j} className="skeleton skeleton--card" />
          ))}
        </div>
      ))}
    </div>
  );
}
