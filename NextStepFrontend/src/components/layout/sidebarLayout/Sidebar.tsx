import { getWorkspaceMe } from '@/services/workspace.service';
import type { Workspace } from '@/types/workspace.type';
import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import '@/assets/styles/sidebar.css';

export default function Sidebar() {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();

  useEffect(() => {
    getWorkspaceMe().then(setWorkspaces).catch(console.error);
  }, []);

  return (
    <div className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      {/* LOGO + TOGGLE */}
      <div className="sidebar-logo">
        {!collapsed && 'NextStep'}
        <button onClick={() => setCollapsed(!collapsed)}>☰</button>
      </div>

      {/* BẢNG */}
      <NavLink
        to="/dashboard"
        className={({ isActive }) => `sidebar-item ${isActive ? 'active' : ''}`}
      >
        <span>Bảng</span>
      </NavLink>

      {/* WORKSPACE */}
      <div className="sidebar-section">
        {!collapsed && (
          <div className="sidebar-section-title">Các Không gian làm việc</div>
        )}

        <div className="sidebar-menu">
          {workspaces.map((ws) => (
            <NavLink
              key={ws.id}
              to={`/workspace/${ws.slug}/home`}
              data-label={ws.name}
              className={`sidebar-item ${
                location.pathname.startsWith(`/workspace/${ws.slug}`)
                  ? 'active'
                  : ''
              }`}
            >
              <span>{ws.name}</span>
            </NavLink>
          ))}
        </div>
      </div>
    </div>
  );
}
