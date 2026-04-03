import { useState} from 'react';
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { Card, Column } from '@/types/card.type';
import { ListUpdateSchema } from '@/schema/card.schema';
import { updateList } from '@/services/lists.service';
import { InlineEdit } from '../shared/SharedComponents';
import { ListMenu } from '../menu/MenuComponents';
import { AddCardForm } from '../form/FormComponents';

// ============================================================
// SORTABLE CARD
// ============================================================

export function SortableCard({
  card,
  onToggleComplete,
  onClick,
}: {
  card: Card;
  listName: string;
  onToggleComplete: (cardId: string, isCompleted: boolean) => void;
  onClick?: () => void;
}) {
  const [saving, setSaving] = useState(false);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: card.id, data: { type: 'card', card } });

  const handleToggle = async (e: React.MouseEvent) => {
    e.stopPropagation(); // Ngăn chặn sự kiện click mở modal khi bấm nút hoàn thành
    if (saving) return;
    setSaving(true);
    try {
      const nextStatus = !card.isCompleted;
      onToggleComplete(card.id, nextStatus);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

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
      onClick={onClick} // Gọi hàm mở modal từ BoardDetail
    >
      <button
        className={`card-complete-toggle ${card.isCompleted ? 'is-done' : ''} ${saving ? 'is-saving' : ''}`}
        onClick={handleToggle}
        disabled={saving}
      >
        {card.isCompleted ? '✓' : ''}
      </button>
      <span
        className={`task-card-title ${card.isCompleted ? 'is-completed' : ''}`}
      >
        {card.title}
      </span>
    </div>
  );
}

// ============================================================
// CARD OVERLAY (drag ghost)
// ============================================================

export function CardOverlay({ card }: { card: Card }) {
  return (
    <div className="task-card is-dragging">
      <button className="card-complete-toggle">
        {card.isCompleted ? '✓' : ''}
      </button>
      <span className="task-card-title">{card.title}</span>
    </div>
  );
}

// ============================================================
// SORTABLE COLUMN
// ============================================================

interface SortableColumnProps {
  col: Column;
  boardSlug: string;
  onRename: (id: string, name: string) => void;
  onAddCard: (colId: string, title: string) => Promise<void>;
  onToggleCardComplete: (cardId: string, isCompleted: boolean) => void;
  onOpenCard: (cardId: string) => void;
  onCopyList: (col: Column) => void;
  onMoveList: (col: Column) => void;
  onMoveAllCards: (col: Column) => void;
  onArchiveList: (colId: string) => void;
}

export function SortableColumn({
  col,
  boardSlug,
  onRename,
  onAddCard,
  onToggleCardComplete,
  onOpenCard,
  onCopyList,
  onMoveList,
  onMoveAllCards,
  onArchiveList,
}: SortableColumnProps) {
  const [addingCard, setAddingCard] = useState(false);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: col.id, data: { type: 'column', col } });

  const handleRename = async (newName: string) => {
    const parsed = ListUpdateSchema.safeParse({ name: newName });
    if (!parsed.success) return;
    try {
      await updateList(boardSlug, col.id, { name: parsed.data.name });
      onRename(col.id, parsed.data.name);
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddCard = async (title: string) => {
    await onAddCard(col.id, title);
    setAddingCard(false);
  };

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
            onSave={handleRename}
            className="column-title"
            inputClassName="column-title-input"
          />
        </div>
        <ListMenu
          col={col}
          onAddCard={() => setAddingCard(true)}
          onCopy={() => onCopyList(col)}
          onMove={() => onMoveList(col)}
          onMoveAllCards={() => onMoveAllCards(col)}
          onArchive={() => onArchiveList(col.id)}
        />
      </div>

      <SortableContext
        items={col.cards.map((c) => c.id)}
        strategy={verticalListSortingStrategy}
      >
        <div className="task-list">
          {col.loading && <div className="skeleton--card" />}
          {col.error && <div className="column-error">⚠ {col.error}</div>}

          {!col.loading &&
            !col.error &&
            col.cards.map((card) => (
              <SortableCard
                key={card.id}
                card={card}
                listName={col.title}
                onToggleComplete={onToggleCardComplete}
                onClick={() => onOpenCard(card.id)}
              />
            ))}
        </div>
      </SortableContext>

      {addingCard ? (
        <AddCardForm
          onAdd={handleAddCard}
          onCancel={() => setAddingCard(false)}
        />
      ) : (
        <button className="add-task" onClick={() => setAddingCard(true)}>
          + Thêm thẻ
        </button>
      )}
    </div>
  );
}

// ============================================================
// COLUMN OVERLAY
// ============================================================

export function ColumnOverlay({ col }: { col: Column }) {
  return (
    <div className="board-column is-col-dragging">
      <div className="column-header">
        <span className="column-title">{col.title}</span>
        <button className="column-menu-btn">···</button>
      </div>
      <div className="task-list">
        {col.cards.map((c) => (
          <div key={c.id} className="task-card">
            {c.title}
          </div>
        ))}
      </div>
    </div>
  );
}