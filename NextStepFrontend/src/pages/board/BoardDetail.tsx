import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams } from 'react-router-dom';
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  closestCorners,
  type DragStartEvent,
  type DragOverEvent,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
  horizontalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import '@/assets/styles/BoardDetail.css';
import { getBoardDetail, updateBoard } from '@/services/board.service';
import { createList } from '@/services/lists.service';

// ===== Types =====
type Visibility = 'PRIVATE' | 'PUBLIC' | 'WORKSPACE';
type Task = { id: string; title: string };
type Column = { id: string; title: string; tasks: Task[] };

interface BoardState {
  name: string;
  visibility: Visibility;
  backgroundColor?: string;
  backgroundImageUrl?: string;
}

// ===== Visibility config =====
const VISIBILITY_OPTIONS: {
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

// ===== useDebounce =====
function useDebounce<T>(value: T, delay: number): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

// ===== InlineEdit =====
function InlineEdit({
  value,
  onSave,
  className,
  inputClassName,
  multiline = false,
}: {
  value: string;
  onSave: (v: string) => void;
  className?: string;
  inputClassName?: string;
  multiline?: boolean;
}) {
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
      {value || <em style={{ opacity: 0.4 }}>Nhấp đôi để thêm...</em>}
    </span>
  );
}

// ===== VisibilityDropdown =====
function VisibilityDropdown({
  value,
  onChange,
}: {
  value: Visibility;
  onChange: (v: Visibility) => void;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const h = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node))
        setOpen(false);
    };
    document.addEventListener('mousedown', h);
    return () => document.removeEventListener('mousedown', h);
  }, []);

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

// ===== AddListCard — button + inline form cuối dãy cột =====
function AddListCard({ onAdd }: { onAdd: (name: string) => Promise<void> }) {
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
      // keep form open on error so user can retry
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

// ===== SortableTask =====
function SortableTask({ task }: { task: Task }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: task.id, data: { type: 'task', task } });
  return (
    <div
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.4 : 1,
      }}
      className="task-card"
      {...attributes}
      {...listeners}
    >
      {task.title}
    </div>
  );
}
function TaskOverlay({ task }: { task: Task }) {
  return <div className="task-card is-dragging">{task.title}</div>;
}

// ===== SortableColumn =====
function SortableColumn({
  col,
  onRename,
}: {
  col: Column;
  onRename: (id: string, name: string) => void;
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: col.id, data: { type: 'column', col } });
  return (
    <div
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.45 : 1,
      }}
      className="board-column"
    >
      <div className="column-header">
        <div className="column-drag-handle" {...attributes} {...listeners}>
          <InlineEdit
            value={col.title}
            onSave={(v) => onRename(col.id, v)}
            className="column-title"
            inputClassName="column-title-input"
          />
        </div>
        <button className="column-menu-btn" title="Tuỳ chọn list">
          ···
        </button>
      </div>
      <SortableContext
        items={col.tasks.map((t) => t.id)}
        strategy={verticalListSortingStrategy}
      >
        <div className="task-list">
          {col.tasks.map((task) => (
            <SortableTask key={task.id} task={task} />
          ))}
        </div>
      </SortableContext>
      <button className="add-task">+ Thêm thẻ</button>
    </div>
  );
}
function ColumnOverlay({ col }: { col: Column }) {
  return (
    <div className="board-column is-col-dragging">
      <div className="column-header">
        <span className="column-title">{col.title}</span>
        <button className="column-menu-btn">···</button>
      </div>
      <div className="task-list">
        {col.tasks.map((t) => (
          <div key={t.id} className="task-card">
            {t.title}
          </div>
        ))}
      </div>
    </div>
  );
}

// ===== Skeleton =====
function BoardSkeleton() {
  return (
    <div className="board-columns">
      {[1, 2, 3].map((i) => (
        <div key={i} className="board-column board-column--skeleton">
          <div className="skeleton skeleton--title" />
          {[1, 2].map((j) => (
            <div key={j} className="skeleton skeleton--card" />
          ))}
        </div>
      ))}
    </div>
  );
}

// ============================================================
// ===== BoardDetail =====
// ============================================================
export default function BoardDetail() {
  const { boardSlug } = useParams<{ boardSlug: string }>();

  const [columns, setColumns] = useState<Column[]>([]);
  const [board, setBoard] = useState<BoardState>({
    name: '',
    visibility: 'PRIVATE',
  });

  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const [activeColumn, setActiveColumn] = useState<Column | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const savedBoard = useRef<BoardState>(board);
  // FIX: chỉ enable auto-save SAU KHI loadBoard() đã ghi savedBoard.current
  const boardReady = useRef(false);

  // ===== Fetch =====
  const loadBoard = useCallback(async () => {
    if (!boardSlug) return;
    setLoading(true);
    setError(null);
    boardReady.current = false; // reset mỗi lần reload
    try {
      const data = await getBoardDetail(boardSlug);
      const loaded: BoardState = {
        name: data.name,
        visibility: (data.visibility as Visibility) ?? 'PRIVATE',
        backgroundColor: data.backgroundColor,
        backgroundImageUrl: data.backgroundImageUrl,
      };
      setBoard(loaded);
      savedBoard.current = loaded; // snapshot để so sánh
      setColumns(
        data.lists.items
          .filter((l) => !l.isArchived)
          .map((l) => ({ id: String(l.id), title: l.name, tasks: [] }))
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi không xác định');
    } finally {
      setLoading(false);
      boardReady.current = true; // cho phép auto-save từ đây
    }
  }, [boardSlug]);

  useEffect(() => {
    loadBoard();
  }, [loadBoard]);

  // ===== Auto-save board changes (debounced 800ms) =====
  const debouncedBoard = useDebounce(board, 800);

  useEffect(() => {
    // FIX: chỉ chạy khi board đã được load xong (không phải lúc mount)
    if (!boardReady.current || !boardSlug) return;

    const prev = savedBoard.current;
    const changed =
      prev.name !== debouncedBoard.name ||
      prev.visibility !== debouncedBoard.visibility ||
      prev.backgroundColor !== debouncedBoard.backgroundColor ||
      prev.backgroundImageUrl !== debouncedBoard.backgroundImageUrl;

    if (!changed) return;

    const patch = async () => {
      setSaving(true);
      try {
        await updateBoard(boardSlug, {
          name: debouncedBoard.name,
          visibility: debouncedBoard.visibility,
          backgroundColor: debouncedBoard.backgroundColor,
          backgroundImageUrl: debouncedBoard.backgroundImageUrl,
        });
        savedBoard.current = debouncedBoard;
      } catch {
        // TODO: show toast
      } finally {
        setSaving(false);
      }
    };
    patch();
  }, [debouncedBoard, boardSlug]);

  // ===== Helpers to update board fields =====
  const patchBoard = (partial: Partial<BoardState>) =>
    setBoard((prev) => ({ ...prev, ...partial }));

  // ===== Add list =====
  const handleAddList = async (name: string) => {
    if (!boardSlug) return;
    // FIX: guard null — nếu API trả thiếu field thì fallback an toàn
    const newList = await createList(boardSlug, {
      name,
      beforeId: null,
      afterId: null,
    });
    const listId =
      newList?.id != null ? String(newList.id) : `tmp-${Date.now()}`;
    const listName = newList?.name ?? name;
    setColumns((prev) => [...prev, { id: listId, title: listName, tasks: [] }]);
  };

  // ===== Background style =====
  // FIX: khi backgroundImageUrl null thì hiện backgroundColor, không hardcode ảnh mặc định
  const bgStyle: React.CSSProperties = board.backgroundImageUrl
    ? {
        backgroundImage: `url('${board.backgroundImageUrl}')`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }
    : board.backgroundColor
      ? { backgroundImage: 'none', backgroundColor: board.backgroundColor }
      : { backgroundImage: 'none', backgroundColor: '#1d2125' };

  // ===== DnD =====
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );
  const findColumn = (taskId: string) =>
    columns.find((col) => col.tasks.some((t) => t.id === taskId));

  const handleDragStart = (e: DragStartEvent) => {
    if (e.active.data.current?.type === 'task')
      setActiveTask(e.active.data.current.task);
    if (e.active.data.current?.type === 'column')
      setActiveColumn(e.active.data.current.col);
  };

  const handleDragOver = (e: DragOverEvent) => {
    const { active, over } = e;
    if (!over || active.data.current?.type !== 'task') return;
    const activeId = String(active.id),
      overId = String(over.id);
    if (activeId === overId) return;
    const activeCol = findColumn(activeId);
    const overCol = findColumn(overId) ?? columns.find((c) => c.id === overId);
    if (!activeCol || !overCol || activeCol.id === overCol.id) return;
    setColumns((prev) => {
      const moved = activeCol.tasks.find((t) => t.id === activeId)!;
      const idx = overCol.tasks.findIndex((t) => t.id === overId);
      const ins = idx >= 0 ? idx : overCol.tasks.length;
      return prev.map((col) => {
        if (col.id === activeCol.id)
          return { ...col, tasks: col.tasks.filter((t) => t.id !== activeId) };
        if (col.id === overCol.id) {
          const t = [...col.tasks];
          t.splice(ins, 0, moved);
          return { ...col, tasks: t };
        }
        return col;
      });
    });
  };

  const handleDragEnd = (e: DragEndEvent) => {
    const { active, over } = e;
    setActiveTask(null);
    setActiveColumn(null);
    if (!over) return;
    const activeId = String(active.id),
      overId = String(over.id);
    if (activeId === overId) return;
    if (active.data.current?.type === 'column') {
      setColumns((prev) => {
        const oi = prev.findIndex((c) => c.id === activeId),
          ni = prev.findIndex((c) => c.id === overId);
        return oi !== -1 && ni !== -1 ? arrayMove(prev, oi, ni) : prev;
      });
      return;
    }
    setColumns((prev) =>
      prev.map((col) => {
        const oi = col.tasks.findIndex((t) => t.id === activeId),
          ni = col.tasks.findIndex((t) => t.id === overId);
        return oi !== -1 && ni !== -1
          ? { ...col, tasks: arrayMove(col.tasks, oi, ni) }
          : col;
      })
    );
  };

  // ===== Render =====
  return (
    <div className="board-container" style={bgStyle}>
      {/* ========== HEADER ========== */}
      <div className="board-header-bar">
        <div className="board-header-inner">
          {/* LEFT — name + visibility */}
          <div className="board-header-left">
            {loading ? (
              <div className="skeleton skeleton--board-title" />
            ) : (
              <InlineEdit
                value={board.name || boardSlug || ''}
                onSave={(v) => patchBoard({ name: v })}
                className="board-title"
                inputClassName="board-title-input"
              />
            )}
            <VisibilityDropdown
              value={board.visibility}
              onChange={(v) => patchBoard({ visibility: v })}
            />
            {saving && <span className="board-saving-hint">Đang lưu…</span>}
          </div>

          {/* RIGHT — share + more */}
          <div className="board-header-right">
            <button className="header-btn header-btn--share">⊕ Chia sẻ</button>
            <button
              className="header-btn header-btn--more"
              title="Tuỳ chọn thêm"
            >
              ···
            </button>
          </div>
        </div>
      </div>

      {/* Error banner */}
      {error && (
        <div className="board-error-banner">
          <span>⚠ {error}</span>
          <button onClick={loadBoard}>Thử lại</button>
        </div>
      )}

      {/* ========== COLUMNS ========== */}
      <div className="board-scroll-area">
        {loading ? (
          <BoardSkeleton />
        ) : (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragStart={handleDragStart}
            onDragOver={handleDragOver}
            onDragEnd={handleDragEnd}
          >
            <SortableContext
              items={columns.map((c) => c.id)}
              strategy={horizontalListSortingStrategy}
            >
              <div className="board-columns">
                {columns.map((col) => (
                  <SortableColumn
                    key={col.id}
                    col={col}
                    onRename={(id, name) =>
                      setColumns((prev) =>
                        prev.map((c) =>
                          c.id === id ? { ...c, title: name } : c
                        )
                      )
                    }
                  />
                ))}

                {/* Thêm danh sách mới — luôn hiện cuối dãy */}
                <AddListCard onAdd={handleAddList} />
              </div>
            </SortableContext>

            <DragOverlay>
              {activeTask && <TaskOverlay task={activeTask} />}
              {activeColumn && <ColumnOverlay col={activeColumn} />}
            </DragOverlay>
          </DndContext>
        )}
      </div>
    </div>
  );
}
