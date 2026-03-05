import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe } from '@/services/user.service';
import type { User } from '@/types/user.type';
import '@/assets/styles/profile.css';

export default function ProfilePage() {
  const [user, setUser] = useState<User | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    getMe().then(setUser).catch(console.error);
  }, []);

  if (!user) return <p className="profile-loading">Loading...</p>;

  return (
    <div className="profile-wrapper">
      <div className="profile-card">
        {/* Nút X */}
        <button
          className="profile-close"
          onClick={() => navigate('/dashboard')}
        >
          ✕
        </button>

        <div className="profile-header">
          <div className="profile-avatar">
            {user.fullName.charAt(0).toUpperCase()}
          </div>

          <div>
            <div className="profile-name">{user.fullName}</div>
            <div className="profile-role">{user.role}</div>
          </div>
        </div>

        <div className="profile-grid">
          <div className="profile-item">
            <span>Username</span>
            <p>{user.username}</p>
          </div>

          <div className="profile-item">
            <span>Email</span>
            <p>{user.email}</p>
          </div>

          <div className="profile-item">
            <span>Phone</span>
            <p>{user.phone || '—'}</p>
          </div>

          <div className="profile-item">
            <span>Status</span>
            <p className={user.isActive ? 'active' : 'inactive'}>
              {user.isActive ? 'Active' : 'Inactive'}
            </p>
          </div>

          <div className="profile-item">
            <span>Created At</span>
            <p>{new Date(user.audit.createdAt).toLocaleString()}</p>
          </div>

          <div className="profile-item">
            <span>Updated At</span>
            <p>{new Date(user.audit.updatedAt).toLocaleString()}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
