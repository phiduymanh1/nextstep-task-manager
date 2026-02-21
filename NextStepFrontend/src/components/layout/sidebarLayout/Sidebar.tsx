import '@/components/layout/sidebarLayout/Sidebar.css';

export default function Sidebar() {
  return (
    <div className="sidebar">
      <div className="sidebar-logo">NextStep</div>

      <nav className="sidebar-menu">
        <div className="sidebar-item">Bảng</div>
        <div className="sidebar-item">Mẫu</div>
        <div className="sidebar-item">Trang chủ</div>
      </nav>

      <div className="sidebar-section">
        <div className="sidebar-section-title">Các Không gian làm việc</div>

        <div className="sidebar-menu">
          <div className="sidebar-item">Đồ án tốt nghiệp</div>

          <div className="sidebar-item">Lịch Trình</div>

          <div className="sidebar-item active">Luyện tập chuyên ngành</div>
        </div>
      </div>
    </div>
  );
}
