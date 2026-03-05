import Sidebar from '@/components/layout/sidebarLayout/Sidebar';
import Navbar from '@/components/layout/navbarLayout/Navbar';
import BoardCard from '@/components/layout/boardCardLayout/BoardCard';

export default function Dashboard() {
  return (
    <div className="dashboard">
      <Sidebar />

      <div className="dashboard-main">
        <Navbar />

        <div className="dashboard-content">
          <div className="workspace-header">
            <div className="workspace-icon">L</div>

            <div>
              <div className="workspace-title">Luyện tập chuyên ngành</div>
              <div className="workspace-privacy">Riêng tư</div>
            </div>
          </div>

          <div className="section-title">Các bảng của bạn</div>

          <div className="board-grid">
            <BoardCard
              title="Lộ trình học trước thực tập - Mô hình Sprint"
              image="https://images.unsplash.com/photo-1501785888041-af3ef285b470"
            />

            <div className="create-board">Tạo bảng mới</div>
          </div>
        </div>
      </div>
    </div>
  );
}
