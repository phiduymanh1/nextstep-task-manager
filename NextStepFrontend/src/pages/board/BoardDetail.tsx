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
  horizontalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable';

import '@/assets/styles/BoardDetail.css';
import '@/assets/styles/CardDetailModal.css';
import { getBoardDetail, updateBoard } from '@/services/board.service';
import type { BoardState, Card, Column } from '@/types/card.type';
import type { Visibility } from '@/types/workspace.type';
import {
  createList,
  getListDetail,
  updateListPosition,
} from '@/services/lists.service';
import { useDebounce } from '@/hook/Hooks';
import { createCard, updateCardPosition } from '@/services/card.service';
import { CardPositionSchema, ListPositionSchema } from '@/schema/card.schema';
import {
  BoardSkeleton,
  InlineEdit,
  VisibilityDropdown,
} from '@/components/shared/SharedComponents';
import { BoardMenu } from '@/components/menu/MenuComponents';
import {
  CardOverlay,
  ColumnOverlay,
  SortableColumn,
} from '@/components/colunm/ColumnComponents';
import { AddListCard } from '@/components/form/FormComponents';
import toast from 'react-hot-toast';
import type { ApiResponse } from '@/types/api.type';
import axios from 'axios';
import CardDetailModal from '../card/CardDetailModal';
import ShareBoardModal from '../share/ShareBoardModal';

// ============================================================
export default function BoardDetail() {
  const { slug, boardSlug } = useParams<{
    slug: string;
    boardSlug: string;
  }>();

  const [columns, setColumns] = useState<Column[]>([]);
  const [showShareModal, setShowShareModal] = useState(false);
  const [board, setBoard] = useState<BoardState>({
    name: '',
    visibility: 'PRIVATE',
  });

  const [activeCard, setActiveCard] = useState<Card | null>(null);
  const [activeColumn, setActiveColumn] = useState<Column | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [selectedCardId, setSelectedCardId] = useState<string | null>(null);

  const savedBoard = useRef<BoardState>(board);
  const boardReady = useRef(false);

  // ===== Load board + lists =====
  const loadBoard = useCallback(async () => {
    if (!boardSlug) return;
    setLoading(true);
    setError(null);
    boardReady.current = false;
    try {
      const data = await getBoardDetail(boardSlug);
      const loaded: BoardState = {
        name: data.name,
        visibility: (data.visibility as Visibility) ?? 'PRIVATE',
        backgroundColor: data.backgroundColor,
        backgroundImageUrl: data.backgroundImageUrl,
      };
      setBoard(loaded);
      savedBoard.current = loaded;

      type ListDTO = { id: number; name: string; isArchived: boolean };
      const initialCols: Column[] = data.lists.items
        .filter((l: ListDTO) => !l.isArchived)
        .map((l: ListDTO) => ({
          id: String(l.id),
          title: l.name,
          cards: [],
          loading: true,
          error: null,
        }));
      setColumns(initialCols);

      initialCols.forEach((col) => loadCardsForColumn(col.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi không xác định');
    } finally {
      setLoading(false);
      boardReady.current = true;
    }
  }, [boardSlug]);

  const loadCardsForColumn = async (colId: string) => {
    try {
      const detail = await getListDetail(colId);
      setColumns((prev) =>
        prev.map((c) =>
          c.id === colId
            ? {
                ...c,
                loading: false,
                error: null,
                cards: detail.cards.map((card) => ({
                  id: String(card.id),
                  title: card.title,
                  description: card.description,
                  isCompleted: card.isCompleted ?? false, // <-- THÊM DÒNG NÀY
                })),
              }
            : c
        )
      );
    } catch (err) {
      setColumns((prev) =>
        prev.map((c) =>
          c.id === colId
            ? {
                ...c,
                loading: false,
                error:
                  err instanceof Error ? err.message : 'Không tải được thẻ',
              }
            : c
        )
      );
    }
  };

  useEffect(() => {
    loadBoard();
  }, [loadBoard]);

  // ===== Auto-save board (debounced 800ms) =====
  const debouncedBoard = useDebounce(board, 800);

  useEffect(() => {
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
      } catch (error: unknown) {
        if (axios.isAxiosError<ApiResponse>(error)) {
          const status = error.response?.status;
          const meta = error.response?.data?.metaData;

          const errors =
            meta?.errors && meta.errors.length > 0
              ? meta.errors
              : [meta?.message || 'Có lỗi xảy ra'];

          if (status === 400) {
            errors.forEach((err) => toast.error(err));
            return;
          }

          toast.error(meta?.message || 'Lỗi hệ thống');
        } else {
          toast.error('Unexpected error');
        }
      } finally {
        setSaving(false);
      }
    };
    patch();
  }, [debouncedBoard, boardSlug]);

  const patchBoard = (partial: Partial<BoardState>) =>
    setBoard((prev) => ({ ...prev, ...partial }));

  // ===== Add list =====
  const handleAddList = async (name: string) => {
    if (!boardSlug) return;
    const newList = await createList(boardSlug, {
      name,
      beforeId: null,
      afterId: null,
    });
    if (!newList?.id) throw new Error('Tạo danh sách thất bại');
    setColumns((prev) => [
      ...prev,
      {
        id: String(newList.id),
        title: newList.name,
        cards: [],
        loading: false,
        error: null,
      },
    ]);
  };

  // ===== Add card =====
  const handleAddCard = async (colId: string, title: string) => {
    const newCard = await createCard(colId, { title });
    if (!newCard?.id) throw new Error('Tạo thẻ thất bại');
    setColumns((prev) =>
      prev.map((c) =>
        c.id === colId
          ? {
              ...c,
              cards: [
                ...c.cards,
                {
                  id: String(newCard.id),
                  title: newCard.title,
                  isCompleted: false, // <-- THÊM DÒNG NÀY
                },
              ],
            }
          : c
      )
    );
  };

  // ===== Toggle card completion — CẬP NHẬT STATE TOÀN CỤC =====
  const handleToggleCardComplete = (cardId: string, isCompleted: boolean) => {
    setColumns((prev) =>
      prev.map((col) => ({
        ...col,
        cards: col.cards.map((card) =>
          card.id === cardId ? { ...card, isCompleted } : card
        ),
      }))
    );
  };

  // ===== List menu actions =====
  const handleCopyList = (col: Column) => console.log('Copy list:', col.id);
  const handleMoveList = (col: Column) => console.log('Move list:', col.id);
  const handleMoveAllCards = (col: Column) =>
    console.log('Move all cards:', col.id);
  const handleArchiveList = (colId: string) => {
    setColumns((prev) => prev.filter((c) => c.id !== colId));
  };

  // ===== Board menu actions =====
  const handleChangeBg = () => console.log('Change background');
  const handleCloseBoard = () => console.log('Close board');

  // ===== Background =====
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

  const findColumnByCardId = (cardId: string) =>
    columns.find((col) => col.cards.some((c) => c.id === cardId));

  const handleDragStart = (e: DragStartEvent) => {
    if (e.active.data.current?.type === 'card')
      setActiveCard(e.active.data.current.card);
    if (e.active.data.current?.type === 'column')
      setActiveColumn(e.active.data.current.col);
  };

  const handleDragOver = (e: DragOverEvent) => {
    const { active, over } = e;
    if (!over || active.data.current?.type !== 'card') return;
    const activeId = String(active.id);
    const overId = String(over.id);
    if (activeId === overId) return;

    const activeCol = findColumnByCardId(activeId);
    const overCol =
      findColumnByCardId(overId) ?? columns.find((c) => c.id === overId);
    if (!activeCol || !overCol || activeCol.id === overCol.id) return;

    setColumns((prev) => {
      const moved = activeCol.cards.find((c) => c.id === activeId)!;
      const idx = overCol.cards.findIndex((c) => c.id === overId);
      const ins = idx >= 0 ? idx : overCol.cards.length;
      return prev.map((col) => {
        if (col.id === activeCol.id)
          return { ...col, cards: col.cards.filter((c) => c.id !== activeId) };
        if (col.id === overCol.id) {
          const cards = [...col.cards];
          cards.splice(ins, 0, moved);
          return { ...col, cards };
        }
        return col;
      });
    });
  };

  const handleDragEnd = async (e: DragEndEvent) => {
    const { active, over } = e;
    setActiveCard(null);
    setActiveColumn(null);
    if (!over) return;
    const activeId = String(active.id);
    const overId = String(over.id);
    if (activeId === overId) return;

    // Column reorder
    if (active.data.current?.type === 'column') {
      setColumns((prev) => {
        const oldIdx = prev.findIndex((c) => c.id === activeId);
        const newIdx = prev.findIndex((c) => c.id === overId);
        if (oldIdx === -1 || newIdx === -1) return prev;
        const reordered = arrayMove(prev, oldIdx, newIdx);
        const beforeId =
          newIdx > 0 ? parseInt(reordered[newIdx - 1].id, 10) : null;
        const afterId =
          newIdx < reordered.length - 1
            ? parseInt(reordered[newIdx + 1].id, 10)
            : null;
        if (boardSlug) {
          const payload = ListPositionSchema.parse({ beforeId, afterId });
          updateListPosition(boardSlug, activeId, payload).catch((err) =>
            console.error('Failed to update list position:', err)
          );
        }
        return reordered;
      });
      return;
    }

    // Card reorder within same column
    if (active.data.current?.type === 'card') {
      setColumns((prev) => {
        const col = prev.find(
          (c) =>
            c.cards.some((card) => card.id === activeId) &&
            c.cards.some((card) => card.id === overId)
        );
        if (!col) return prev;

        const oldIdx = col.cards.findIndex((c) => c.id === activeId);
        const newIdx = col.cards.findIndex((c) => c.id === overId);
        if (oldIdx === -1 || newIdx === -1) return prev;

        const reordered = arrayMove(col.cards, oldIdx, newIdx);
        const beforeId =
          newIdx > 0 ? parseInt(reordered[newIdx - 1].id, 10) : null;
        const afterId =
          newIdx < reordered.length - 1
            ? parseInt(reordered[newIdx + 1].id, 10)
            : null;

        const payload = CardPositionSchema.parse({
          listId: parseInt(col.id, 10),
          beforeId,
          afterId,
        });
        updateCardPosition(activeId, payload).catch((err) =>
          console.error('Failed to update card position:', err)
        );

        return prev.map((c) =>
          c.id === col.id ? { ...c, cards: reordered } : c
        );
      });
    }
  };

  // ===== Render =====
  return (
    <div className="board-container" style={bgStyle}>
      {/* ========== HEADER ========== */}
      <div className="board-header-bar">
        <div className="board-header-inner">
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

          <div className="board-header-right">
            <button
              className="header-btn header-btn--share"
              onClick={() => setShowShareModal(true)}
            >
              ⊕ Chia sẻ
            </button>
            <BoardMenu
              visibility={board.visibility}
              onVisibilityChange={(v) => patchBoard({ visibility: v })}
              onChangeBg={handleChangeBg}
              onCloseBoard={handleCloseBoard}
            />
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
                    boardSlug={boardSlug ?? ''}
                    onRename={(id, name) =>
                      setColumns((prev) =>
                        prev.map((c) =>
                          c.id === id ? { ...c, title: name } : c
                        )
                      )
                    }
                    onAddCard={handleAddCard}
                    onToggleCardComplete={handleToggleCardComplete}
                    onOpenCard={(cardId) => setSelectedCardId(cardId)}
                    onCopyList={handleCopyList}
                    onMoveList={handleMoveList}
                    onMoveAllCards={handleMoveAllCards}
                    onArchiveList={handleArchiveList}
                  />
                ))}
                <AddListCard onAdd={handleAddList} />
              </div>
            </SortableContext>

            <DragOverlay>
              {activeCard && <CardOverlay card={activeCard} />}
              {activeColumn && <ColumnOverlay col={activeColumn} />}
            </DragOverlay>
          </DndContext>
        )}
        {selectedCardId && (
          <CardDetailModal
            cardId={selectedCardId}
            onClose={() => setSelectedCardId(null)}
            onToggleComplete={handleToggleCardComplete}
          />
        )}
        {showShareModal && boardSlug && (
          <ShareBoardModal
            boardSlug={boardSlug}
          workspaceSlug={slug!}
            onClose={() => setShowShareModal(false)}
          />
        )}
      </div>
    </div>
  );
}
