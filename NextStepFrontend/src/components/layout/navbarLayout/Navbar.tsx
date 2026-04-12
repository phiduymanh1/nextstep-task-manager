import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '@/assets/styles/Navbar.css';
import { handleLogout } from '@/utils/auth';
import CreateWorkspaceModal from '@/pages/workspace/CreateWorkspaceModal';

export default function Navbar() {
  const navigate = useNavigate();
  const [openUser, setOpenUser] = useState(false);
  const [openCreate, setOpenCreate] = useState(false);
  const [openWorkspaceModal, setOpenWorkspaceModal] = useState(false);

  const userRef = useRef<HTMLDivElement>(null);
  const createRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (userRef.current && !userRef.current.contains(event.target as Node)) {
        setOpenUser(false);
      }
      if (
        createRef.current &&
        !createRef.current.contains(event.target as Node)
      ) {
        setOpenCreate(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <>
      <div className="navbar">
        <input placeholder="Tìm kiếm" className="navbar-search" />

        {/* navbar-right KHÔNG có position:relative */}
        <div className="navbar-right">
          {/* CREATE — wrapper có position:relative */}
          <div className="navbar-create" ref={createRef}>
            <button
              className="navbar-button"
              onClick={() => setOpenCreate((prev) => !prev)}
            >
              Tạo mới
            </button>

            {openCreate && (
              <div className="navbar-dropdown">
                <button
                  onClick={() => {
                    setOpenWorkspaceModal(true);
                    setOpenCreate(false);
                  }}
                >
                  🏢 Tạo Workspace
                </button>
              </div>
            )}
          </div>

          {/* AVATAR — wrapper có position:relative */}
          <div className="navbar-avatar" ref={userRef}>
            <div onClick={() => setOpenUser((prev) => !prev)}>M</div>

            {openUser && (
              <div className="navbar-dropdown">
                <button
                  onClick={() => {
                    navigate('/profile');
                    setOpenUser(false);
                  }}
                >
                  Profile
                </button>
                <button
                  onClick={() => {
                    setOpenWorkspaceModal(true);
                    setOpenUser(false);
                  }}
                >
                  Create Workspace
                </button>
                <button onClick={handleLogout}>Logout</button>
              </div>
            )}
          </div>
        </div>
      </div>

      <CreateWorkspaceModal
        open={openWorkspaceModal}
        onClose={() => setOpenWorkspaceModal(false)}
      />
    </>
  );
}
