import {
  getWorkspaceDetailBySlug,
  updateWorkspace,
} from '@/services/workspace.service';
import type {
  Visibility,
  WorkspaceDetailResponse,
} from '@/types/workspace.type';

import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

import '@/assets/styles/WorkspaceHome.css';

import UpdateWorkspaceModal from './UpdateWorkspaceModal';
import CreateBoardModal from '../board/CreateBoardModal';
import type { BoardResponse } from '@/types/board.type';
import WorkspaceMembersModal from '@/pages/workspace/Workspacemembersmodal';

const PAGE_SIZE = 5;

export default function WorkspaceHome() {
  const { slug } = useParams();
  const navigate = useNavigate();

  const [data, setData] = useState<WorkspaceDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const [boards, setBoards] = useState<BoardResponse[]>([]);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);

  const [openEdit, setOpenEdit] = useState(false);
  const [openCreateBoard, setOpenCreateBoard] = useState(false);
  const [openMembers, setOpenMembers] = useState(false);

  const fetchBoards = useCallback(
    async (pageNumber: number) => {
      if (!slug) return;

      const res = await getWorkspaceDetailBySlug(slug, {
        page: pageNumber,
        size: PAGE_SIZE,
      });

      setData(res);

      setBoards((prev) =>
        pageNumber === 0 ? res.boards.items : [...prev, ...res.boards.items]
      );

      setTotal(res.boards.totalElements);
    },
    [slug]
  );

  const handleUpdateWorkspace = async (payload: {
    name: string;
    description?: string;
    visibility: Visibility;
  }) => {
    if (!slug) return;

    await updateWorkspace(slug, payload);

    setPage(0);
    await fetchBoards(0);
  };

  useEffect(() => {
    if (!slug) return;

    const load = async () => {
      try {
        setLoading(true);
        await fetchBoards(0);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [slug, fetchBoards]);

  const getVisibilityInfo = (visibility: Visibility) => {
    switch (visibility) {
      case 'PRIVATE':
        return { icon: '🔒', label: 'Riêng tư' };
      case 'PUBLIC':
        return { icon: '🌐', label: 'Công khai' };
      case 'WORKSPACE':
        return { icon: '👥', label: 'Trong workspace' };
      default:
        return { icon: '❓', label: 'Không xác định' };
    }
  };

  if (loading) return <div>Đang tải workspace...</div>;
  if (!data) return <div>Không tìm thấy workspace</div>;

  const hasMore = boards.length < total;
  const remaining = total - boards.length;

  return (
    <div className="workspace-container">
      {/* Header */}
      <div className="workspace-header">
        <div className="workspace-avatar">
          {data.name.charAt(0).toUpperCase()}
        </div>

        <div className="workspace-info">
          <div className="workspace-name-row">
            <h1 className="workspace-name">{data.name}</h1>
            <button className="edit-button" onClick={() => setOpenEdit(true)}>
              ✎
            </button>
          </div>

          <div className="workspace-meta">
            {getVisibilityInfo(data.visibility).icon}{' '}
            {getVisibilityInfo(data.visibility).label}
          </div>
        </div>

        {/* Members button */}
        <button className="members-button" onClick={() => setOpenMembers(true)}>
          👥 Thành viên
        </button>
      </div>

      {/* Boards */}
      <h2 className="section-title">Các bảng của bạn</h2>

      {boards.length === 0 ? (
        <div className="empty-state">
          <p className="empty-text">Chưa có board nào</p>
          <button
            className="create-board-btn"
            onClick={() => setOpenCreateBoard(true)}
          >
            + Tạo bảng đầu tiên
          </button>
        </div>
      ) : (
        <>
          <div className="board-grid">
            {boards.map((board) => (
              <div
                key={board.id}
                className="board-card"
                onClick={() =>
                  navigate(`/workspace/${slug}/board/${board.slug}`)
                }
                style={{ cursor: 'pointer' }}
              >
                <div
                  className="board-bg"
                  style={{
                    backgroundImage: board.backgroundImageUrl
                      ? `url(${board.backgroundImageUrl})`
                      : undefined,
                    backgroundColor: !board.backgroundImageUrl
                      ? board.backgroundColor || '#2c2c2c'
                      : undefined,
                    backgroundPosition: 'center',
                    backgroundSize: 'cover',
                    backgroundRepeat: 'no-repeat',
                  }}
                />
                <div className="board-content">
                  <div className="board-name">{board.name}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="board-actions">
            <button
              className="create-board-btn"
              onClick={() => setOpenCreateBoard(true)}
            >
              + Tạo bảng mới
            </button>

            {hasMore && (
              <button
                className="load-more-btn"
                onClick={async () => {
                  const nextPage = page + 1;
                  setPage(nextPage);
                  await fetchBoards(nextPage);
                }}
              >
                Xem thêm ({remaining} bảng)
              </button>
            )}
          </div>
        </>
      )}

      {/* Modals */}
      <UpdateWorkspaceModal
        open={openEdit}
        onClose={() => setOpenEdit(false)}
        defaultData={{
          name: data.name,
          visibility: data.visibility,
        }}
        onSubmit={handleUpdateWorkspace}
      />

      <CreateBoardModal
        open={openCreateBoard}
        onClose={() => setOpenCreateBoard(false)}
        workspaceSlug={slug!}
        onSuccess={async () => {
          setPage(0);
          await fetchBoards(0);
        }}
      />

      <WorkspaceMembersModal
        open={openMembers}
        onClose={() => setOpenMembers(false)}
        workspaceSlug={slug!}
      />
    </div>
  );
}
