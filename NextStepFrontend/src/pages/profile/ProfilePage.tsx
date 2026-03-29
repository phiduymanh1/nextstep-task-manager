import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe, updateMe } from '@/services/user.service';
import type { User } from '@/types/user.type';
import '@/assets/styles/profile.css';
export default function ProfilePage() {
  const [user, setUser] = useState<User | null>(null);
  const [editing, setEditing] = useState(false);

  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    getMe()
      .then((u) => {
        setUser(u);
        setFullName(u.fullName);
        setPhone(u.phone || '');
      })
      .catch(console.error);
  }, []);

  useEffect(() => {
    if (user) {
      console.log('Dữ liệu User đã cập nhật vào State:', user);
    }
  }, [user]);

  const handleSave = async () => {
    try {
      await updateMe({ fullName, phone });

      setUser((prevUser) => {
        if (!prevUser) return prevUser;

        return {
          ...prevUser,
          fullName,
          phone,
        };
      });
      setEditing(false);
    } catch (err) {
      console.error(err);
    }
  };

  if (!user) return <p className="profile-loading">Loading...</p>;

  return (
    <div className="profile-wrapper">
      <div className="profile-card">
        {/* close button */}
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
            {editing ? (
              <input
                className="profile-input"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            ) : (
              <div className="profile-name">{user.fullName}</div>
            )}

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

            {editing ? (
              <input
                className="profile-input"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
            ) : (
              <p>{user.phone || '—'}</p>
            )}
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

        <div className="profile-actions">
          {editing ? (
            <>
              <button className="save-btn" onClick={handleSave}>
                Save
              </button>

              <button
                className="cancel-btn"
                onClick={() => {
                  setEditing(false);
                  setFullName(user.fullName);
                  setPhone(user.phone || '');
                }}
              >
                Cancel
              </button>
            </>
          ) : (
            <button className="edit-btn" onClick={() => setEditing(true)}>
              Edit Profile
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
