import { useState } from 'react';
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


// ===== SortableCard =====
export function SortableCard({ card }: { card: Card }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: card.id, data: { type: 'card', card } });

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
      <span className="task-card-title">{card.title}</span>
    </div>
  );
}

export function CardOverlay({ card }: { card: Card }) {
  return (
    <div className="task-card is-dragging">
      <span className="task-card-title">{card.title}</span>
    </div>
  );
}

// ===== SortableColumn =====
interface SortableColumnProps {
  col: Column;
  boardSlug: string;
  onRename: (id: string, name: string) => void;
  onAddCard: (colId: string, title: string) => Promise<void>;
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
    if (!parsed.success) {
      console.error('Validation error:', parsed.error.issues);
      return;
    }
    try {
      await updateList(boardSlug, col.id, { name: parsed.data.name });
      onRename(col.id, parsed.data.name);
    } catch (err) {
      console.error('Failed to rename list:', err);
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
      {/* Column header */}
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

      {/* Cards list */}
      <SortableContext
        items={col.cards.map((c) => c.id)}
        strategy={verticalListSortingStrategy}
      >
        <div className="task-list">
          {col.loading && (
            <div className="column-loading">
              {[1, 2].map((i) => (
                <div key={i} className="skeleton skeleton--card" />
              ))}
            </div>
          )}

          {col.error && (
            <div className="column-error">
              <span>⚠ {col.error}</span>
            </div>
          )}

          {!col.loading &&
            !col.error &&
            col.cards.map((card) => <SortableCard key={card.id} card={card} />)}
        </div>
      </SortableContext>

      {/* Add card form or button */}
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
