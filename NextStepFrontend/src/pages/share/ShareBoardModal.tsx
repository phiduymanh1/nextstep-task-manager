import { useState, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';
import '@/assets/styles/ShareBoardModal.css';
import {
  addMemberToBoard,
  getBoardMembers,
  removeMemberFromBoard,
  type BoardMemberResponse,
} from '@/services/boardmember.service';
import {
  getWorkspaceMembers,
  type WorkspaceMemberResponse,
} from '@/services/workspacemember.service';

interface Props {
  boardSlug: string;
  workspaceSlug: string;
  onClose: () => void;
}

interface MergedMember extends WorkspaceMemberResponse {
  isInBoard: boolean;
  boardRole: string | null;
}

type Tab = 'board' | 'workspace';

const AVATAR_COLORS = [
  '#e85d04',
  '#7209b7',
  '#3a86ff',
  '#06d6a0',
  '#f72585',
  '#fb8500',
  '#118ab2',
  '#8338ec',
];

function colorFor(name: string): string {
  let h = 0;
  for (const c of name) h = (h * 31 + c.charCodeAt(0)) & 0xffff;
  return AVATAR_COLORS[h % AVATAR_COLORS.length];
}

function initials(name: string): string {
  return name
    .split(' ')
    .map((w) => w[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();
}

export default function ShareBoardModal({
  boardSlug,
  workspaceSlug,
  onClose,
}: Props) {
  type BoardRole = 'ADMIN' | 'MEMBER' | 'OBSERVER';

  const [members, setMembers] = useState<MergedMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<Tab>('board');
  const [search, setSearch] = useState('');
  const [loadingIds, setLoadingIds] = useState<Set<number>>(new Set());
  const [selectedRoles, setSelectedRoles] = useState<Record<number, BoardRole>>(
    {}
  );

  const loadMembers = useCallback(async () => {
    setLoading(true);
    try {
      const [wsMembers, boardMembers]: [
        WorkspaceMemberResponse[],
        BoardMemberResponse[],
      ] = await Promise.all([
        getWorkspaceMembers(workspaceSlug),
        getBoardMembers(boardSlug),
      ]);

      const boardMap = new Map<number, BoardMemberResponse>(
        boardMembers.map((m) => [m.userId, m])
      );

      const merged: MergedMember[] = wsMembers.map((m) => ({
        ...m,
        isInBoard: boardMap.has(m.userId),
        boardRole: boardMap.get(m.userId)?.role ?? null,
      }));

      setMembers(merged);
    } catch {
      toast.error('Không thể tải danh sách thành viên');
    } finally {
      setLoading(false);
    }
  }, [boardSlug, workspaceSlug]);

  useEffect(() => {
    loadMembers();
  }, [loadMembers]);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const setLoadingId = (userId: number, on: boolean) =>
    setLoadingIds((prev) => {
      const next = new Set(prev);

      if (on) {
        next.add(userId);
      } else {
        next.delete(userId);
      }

      return next;
    });

  const handleAdd = async (userId: number) => {
    const role: BoardRole = selectedRoles[userId] || 'MEMBER';

    setLoadingId(userId, true);
    try {
      await addMemberToBoard(boardSlug, { userId, role });

      setMembers((prev) =>
        prev.map((m) =>
          m.userId === userId ? { ...m, isInBoard: true, boardRole: role } : m
        )
      );

      toast.success('Đã thêm thành viên vào bảng');
    } catch {
      toast.error('Thêm thành viên thất bại');
    } finally {
      setLoadingId(userId, false);
    }
  };

  const handleRemove = async (userId: number) => {
    setLoadingId(userId, true);
    try {
      await removeMemberFromBoard(boardSlug, userId);
      setMembers((prev) =>
        prev.map((m) =>
          m.userId === userId ? { ...m, isInBoard: false, boardRole: null } : m
        )
      );
      toast.success('Đã xóa thành viên khỏi bảng');
    } catch {
      toast.error('Xóa thành viên thất bại');
    } finally {
      setLoadingId(userId, false);
    }
  };

  const filtered = members
    .filter((m) => (tab === 'board' ? m.isInBoard : true))
    .filter((m) => {
      if (!search) return true;
      const q = search.toLowerCase();
      return (
        m.fullName?.toLowerCase().includes(q) ||
        m.username?.toLowerCase().includes(q) ||
        m.email?.toLowerCase().includes(q)
      );
    });

  const boardCount = members.filter((m) => m.isInBoard).length;

  return (
    <div
      className="sbm-backdrop"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="sbm-modal">
        {/* Header */}
        <div className="sbm-header">
          <span className="sbm-title">Chia sẻ bảng</span>
          <button className="sbm-close-btn" onClick={onClose}>
            ✕
          </button>
        </div>

        {/* Search */}
        <div className="sbm-search-row">
          <input
            className="sbm-search-input"
            type="text"
            placeholder="Tìm theo tên hoặc email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {/* Tabs */}
        <div className="sbm-tabs">
          <button
            className={`sbm-tab ${tab === 'board' ? 'active' : ''}`}
            onClick={() => setTab('board')}
          >
            Thành viên bảng
            <span className="sbm-tab-badge">{boardCount}</span>
          </button>
          <button
            className={`sbm-tab ${tab === 'workspace' ? 'active' : ''}`}
            onClick={() => setTab('workspace')}
          >
            Workspace
          </button>
        </div>

        {/* List */}
        <div className="sbm-list">
          {loading ? (
            <div className="sbm-loading">
              <div className="sbm-spinner" />
              Đang tải...
            </div>
          ) : filtered.length === 0 ? (
            <div className="sbm-empty">Không tìm thấy thành viên nào</div>
          ) : (
            filtered.map((m) => {
              const isLoading = loadingIds.has(m.userId);
              const isAdmin = m.boardRole === 'ADMIN';

              return (
                <div key={m.userId} className="sbm-member-row">
                  <div
                    className="sbm-avatar"
                    style={{ background: colorFor(m.fullName ?? '') }}
                  >
                    {initials(m.fullName ?? '?')}
                  </div>

                  <div className="sbm-member-info">
                    <span className="sbm-member-name">{m.fullName}</span>
                    <span className="sbm-member-sub">
                      {m.username} ·{' '}
                      {m.workspaceRole ?? 'Khách Không gian làm việc'}
                    </span>
                  </div>

                  <div className="sbm-member-actions">
                    {m.isInBoard && m.boardRole && (
                      <span
                        className={`sbm-role-badge ${
                          m.boardRole === 'ADMIN'
                            ? 'admin'
                            : m.boardRole === 'OBSERVER'
                              ? 'observer'
                              : ''
                        }`}
                      >
                        {m.boardRole === 'ADMIN'
                          ? 'Quản trị viên'
                          : m.boardRole === 'OBSERVER'
                            ? 'Quan sát'
                            : 'Thành viên'}
                      </span>
                    )}

                    {m.isInBoard ? (
                      !isAdmin && (
                        <button
                          className="sbm-btn sbm-btn--remove"
                          disabled={isLoading}
                          onClick={() => handleRemove(m.userId)}
                        >
                          {isLoading ? (
                            <span className="sbm-btn-spinner" />
                          ) : (
                            'Xóa'
                          )}
                        </button>
                      )
                    ) : (
                      <>
                        {/*  Dropdown chọn role */}
                        <select
                          className="sbm-role-select"
                          value={selectedRoles[m.userId] || 'MEMBER'}
                          onChange={(e) =>
                            setSelectedRoles((prev) => ({
                              ...prev,
                              [m.userId]: e.target.value as BoardRole,
                            }))
                          }
                        >
                          <option value="ADMIN">Quản trị viên</option>
                          <option value="MEMBER">Thành viên</option>
                          <option value="OBSERVER">Quan sát</option>
                        </select>

                        {/* Nút thêm */}
                        <button
                          className="sbm-btn sbm-btn--add"
                          disabled={isLoading}
                          onClick={() => handleAdd(m.userId)}
                        >
                          {isLoading ? (
                            <span className="sbm-btn-spinner" />
                          ) : (
                            '+ Thêm'
                          )}
                        </button>
                      </>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
