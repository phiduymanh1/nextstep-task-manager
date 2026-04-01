import { useOnClickOutside } from '@/hook/Hooks';
import { VISIBILITY_OPTIONS, type Column } from '@/types/card.type';
import type { Visibility } from '@/types/workspace.type';
import { useState, useRef } from 'react';

// ===== BoardMenu =====
interface BoardMenuProps {
  visibility: Visibility;
  onVisibilityChange: (v: Visibility) => void;
  onChangeBg: () => void;
  onCloseBoard: () => void;
}

export function BoardMenu({
  visibility,
  onVisibilityChange,
  onChangeBg,
  onCloseBoard,
}: BoardMenuProps) {
  const [open, setOpen] = useState(false);
  const [visSubOpen, setVisSubOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useOnClickOutside(ref as React.RefObject<HTMLElement>, () => {
    setOpen(false);
    setVisSubOpen(false);
  });

  const currentVis = VISIBILITY_OPTIONS.find((o) => o.value === visibility)!;

  return (
    <div className="board-menu-wrap" ref={ref}>
      <button
        className="header-btn header-btn--more"
        title="Tuỳ chọn thêm"
        onClick={() => {
          setOpen((p) => !p);
          setVisSubOpen(false);
        }}
      >
        ···
      </button>

      {open && (
        <div className="board-menu-panel">
          <div className="board-menu-header">
            <span className="board-menu-title">Menu</span>
            <button className="board-menu-close" onClick={() => setOpen(false)}>
              ✕
            </button>
          </div>

          <div className="board-menu-divider" />

          <button className="board-menu-item" onClick={() => {}}>
            <span className="board-menu-item-icon">ℹ️</span>
            <div className="board-menu-item-body">
              <div className="board-menu-item-label">Về bảng này</div>
              <div className="board-menu-item-desc">
                Thêm mô tả vào bảng của bạn
              </div>
            </div>
          </button>

          <button
            className="board-menu-item"
            onClick={() => setVisSubOpen((p) => !p)}
          >
            <span className="board-menu-item-icon">👁️</span>
            <div className="board-menu-item-body">
              <div className="board-menu-item-label">
                Khả năng hiển thị: {currentVis.label}
              </div>
            </div>
            <span className="board-menu-item-arrow">
              {visSubOpen ? '▴' : '▾'}
            </span>
          </button>

          {visSubOpen && (
            <div className="board-menu-sub">
              {VISIBILITY_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  className={`visibility-item${visibility === opt.value ? ' active' : ''}`}
                  onClick={() => {
                    onVisibilityChange(opt.value);
                    setVisSubOpen(false);
                  }}
                >
                  <div className="visibility-item-header">
                    <span>
                      {opt.icon} {opt.label}
                    </span>
                    {visibility === opt.value && (
                      <span className="visibility-check">✓</span>
                    )}
                  </div>
                  <div className="visibility-item-desc">{opt.desc}</div>
                </button>
              ))}
            </div>
          )}

          <button className="board-menu-item" onClick={() => {}}>
            <span className="board-menu-item-icon">📤</span>
            <div className="board-menu-item-body">
              <div className="board-menu-item-label">In, xuất và chia sẻ</div>
            </div>
          </button>

          <div className="board-menu-divider" />

          <button
            className="board-menu-item"
            onClick={() => {
              onChangeBg();
              setOpen(false);
            }}
          >
            <span className="board-menu-item-icon">🖼️</span>
            <div className="board-menu-item-body">
              <div className="board-menu-item-label">Thay đổi hình nền</div>
            </div>
          </button>

          <div className="board-menu-divider" />

          <button
            className="board-menu-item board-menu-item--danger"
            onClick={() => {
              onCloseBoard();
              setOpen(false);
            }}
          >
            <span className="board-menu-item-icon">🗃️</span>
            <div className="board-menu-item-body">
              <div className="board-menu-item-label">Đóng bảng này</div>
            </div>
          </button>
        </div>
      )}
    </div>
  );
}

// ===== ListMenu =====
interface ListMenuProps {
  col: Column;
  onAddCard: () => void;
  onCopy: () => void;
  onMove: () => void;
  onMoveAllCards: () => void;
  onArchive: () => void;
}

export function ListMenu({
  onAddCard,
  onCopy,
  onMove,
  onMoveAllCards,
  onArchive,
}: ListMenuProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useOnClickOutside(ref as React.RefObject<HTMLElement>, () => setOpen(false));

  const close = () => setOpen(false);

  return (
    <div className="list-menu-wrap" ref={ref}>
      <button
        className="column-menu-btn"
        title="Tuỳ chọn list"
        onClick={() => setOpen((p) => !p)}
      >
        ···
      </button>

      {open && (
        <div className="list-menu-panel">
          <div className="list-menu-header">
            <span className="list-menu-title">Thao tác</span>
            <button className="list-menu-close" onClick={close}>
              ✕
            </button>
          </div>
          <div className="list-menu-divider" />
          <button
            className="list-menu-item"
            onClick={() => {
              onAddCard();
              close();
            }}
          >
            Thêm thẻ
          </button>
          <button
            className="list-menu-item"
            onClick={() => {
              onCopy();
              close();
            }}
          >
            Sao chép danh sách
          </button>
          <button
            className="list-menu-item"
            onClick={() => {
              onMove();
              close();
            }}
          >
            Di chuyển danh sách
          </button>
          <button
            className="list-menu-item"
            onClick={() => {
              onMoveAllCards();
              close();
            }}
          >
            Di chuyển tất cả thẻ trong danh sách này
          </button>
          <div className="list-menu-divider" />
          <button
            className="list-menu-item list-menu-item--danger"
            onClick={() => {
              onArchive();
              close();
            }}
          >
            Lưu trữ danh sách này
          </button>
        </div>
      )}
    </div>
  );
}
