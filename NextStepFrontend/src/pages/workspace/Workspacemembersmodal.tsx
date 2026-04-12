import { useEffect, useState, useRef, useCallback } from 'react';
import { searchUsers } from '@/services/user.service';
import {
  addWorkspaceMember,
  getWorkspaceMembers,
  removeWorkspaceMember,
  updateMemberRole,
  type WorkspaceMemberResponse,
} from '@/services/workspacemember.service';
import type { UserSearchResponse } from '@/types/user.type';

export type WorkspaceRole = 'MEMBER' | 'ADMIN' | 'OWNER' | 'GUEST';

const ROLE_OPTIONS: { value: WorkspaceRole; label: string }[] = [
  { value: 'OWNER', label: 'Chủ sở hữu' },
  { value: 'ADMIN', label: 'Quản trị viên' },
  { value: 'MEMBER', label: 'Thành viên' },
  { value: 'GUEST', label: 'Khách' },
];

interface Props {
  open: boolean;
  onClose: () => void;
  workspaceSlug: string;
}

export default function WorkspaceMembersModal({
  open,
  onClose,
  workspaceSlug,
}: Props) {
  const [members, setMembers] = useState<WorkspaceMemberResponse[]>([]);
  const [loadingMembers, setLoadingMembers] = useState(false);

  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<UserSearchResponse[]>([]);
  const [searching, setSearching] = useState(false);

  // role selected per search result user before adding
  const [addRoles, setAddRoles] = useState<Record<number, WorkspaceRole>>({});

  const [pendingAdd, setPendingAdd] = useState<Set<number>>(new Set());
  const [pendingRemove, setPendingRemove] = useState<Set<number>>(new Set());
  const [pendingRoleUpdate, setPendingRoleUpdate] = useState<Set<number>>(
    new Set()
  );

  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const fetchMembers = useCallback(async () => {
    setLoadingMembers(true);
    try {
      const res = await getWorkspaceMembers(workspaceSlug);
      setMembers(res);
    } finally {
      setLoadingMembers(false);
    }
  }, [workspaceSlug]);

  useEffect(() => {
    if (open) {
      fetchMembers();
      setKeyword('');
      setSearchResults([]);
      setAddRoles({});
    }
  }, [open, fetchMembers]);

  useEffect(() => {
    if (!keyword.trim()) {
      setSearchResults([]);
      return;
    }

    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        const res = await searchUsers(keyword, workspaceSlug);
        setSearchResults(res);
        setAddRoles((prev) => {
          const next = { ...prev };
          res.forEach((u) => {
            if (!next[u.id]) next[u.id] = 'MEMBER';
          });
          return next;
        });
      } finally {
        setSearching(false);
      }
    }, 400);
  }, [keyword, workspaceSlug]);

  const handleAdd = async (userId: number) => {
    const role = addRoles[userId] ?? 'MEMBER';
    setPendingAdd((prev) => new Set(prev).add(userId));
    try {
      await addWorkspaceMember(workspaceSlug, userId, role);
      await fetchMembers();
      setSearchResults((prev) => prev.filter((u) => u.id !== userId));
    } finally {
      setPendingAdd((prev) => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  const handleRemove = async (userId: number) => {
    setPendingRemove((prev) => new Set(prev).add(userId));
    try {
      await removeWorkspaceMember(workspaceSlug, userId);
      await fetchMembers();
    } finally {
      setPendingRemove((prev) => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  const handleRoleChange = async (userId: number, role: WorkspaceRole) => {
    // optimistic update
    setMembers((prev) =>
      prev.map((m) => (m.userId === userId ? { ...m, role } : m))
    );
    setPendingRoleUpdate((prev) => new Set(prev).add(userId));
    try {
      await updateMemberRole(workspaceSlug, userId, role);
    } catch {
      await fetchMembers(); // revert on error
    } finally {
      setPendingRoleUpdate((prev) => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  if (!open) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Quản lý thành viên</h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        {/* Search */}
        <div className="member-search-wrapper">
          <input
            className="member-search-input"
            type="text"
            placeholder="Tìm kiếm người dùng để thêm..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          {searching && <span className="member-search-hint">Đang tìm...</span>}
        </div>

        {/* Search results */}
        {searchResults.length > 0 && (
          <div className="member-section">
            <p className="member-section-label">Kết quả tìm kiếm</p>
            {searchResults.map((user) => (
              <div key={user.id} className="member-row">
                <div className="member-avatar">
                  {user.name?.charAt(0).toUpperCase() ?? '?'}
                </div>
                <div className="member-info">
                  <span className="member-name">{user.name}</span>
                  {user.email && (
                    <span className="member-email">{user.email}</span>
                  )}
                </div>
                <select
                  className="role-select"
                  value={addRoles[user.id] ?? 'MEMBER'}
                  onChange={(e) =>
                    setAddRoles((prev) => ({
                      ...prev,
                      [user.id]: e.target.value as WorkspaceRole,
                    }))
                  }
                >
                  {ROLE_OPTIONS.map((r) => (
                    <option key={r.value} value={r.value}>
                      {r.label}
                    </option>
                  ))}
                </select>
                <button
                  className="member-action-btn add-btn"
                  disabled={pendingAdd.has(user.id)}
                  onClick={() => handleAdd(user.id)}
                >
                  {pendingAdd.has(user.id) ? '...' : '+ Thêm'}
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Current members */}
        <div className="member-section member-section-scroll">
          <p className="member-section-label">
            Thành viên hiện tại ({members.length})
          </p>
          {loadingMembers ? (
            <p className="member-loading">Đang tải...</p>
          ) : members.length === 0 ? (
            <p className="member-empty">Chưa có thành viên nào</p>
          ) : (
            members.map((m) => {
              const isOwner = m.role === 'OWNER';

              return (
                <div key={m.userId} className="member-row">
                  <div className="member-avatar">
                    {m.fullName?.charAt(0).toUpperCase() ?? '?'}
                  </div>
                  <div className="member-info">
                    <span className="member-name">{m.fullName}</span>
                    {m.email && <span className="member-email">{m.email}</span>}
                  </div>

                  {/* Role control */}
                  {isOwner ? (
                    <span className="member-role-badge owner-badge">Owner</span>
                  ) : (
                    <select
                      className="role-select"
                      value={m.role as WorkspaceRole}
                      disabled={pendingRoleUpdate.has(m.userId)}
                      onChange={(e) =>
                        handleRoleChange(
                          m.userId,
                          e.target.value as WorkspaceRole
                        )
                      }
                    >
                      {ROLE_OPTIONS.map((r) => (
                        <option key={r.value} value={r.value}>
                          {r.label}
                        </option>
                      ))}
                    </select>
                  )}

                  {/* Remove button — not for owner or self */}
                  {!isOwner && (
                    <button
                      className="member-action-btn remove-btn"
                      disabled={pendingRemove.has(m.userId)}
                      onClick={() => handleRemove(m.userId)}
                    >
                      {pendingRemove.has(m.userId) ? '...' : 'Xóa'}
                    </button>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
