import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '@/components/layout/navbarLayout/navbar.css';

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const dropdownRef = useRef<HTMLDivElement>(null);

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    navigate('/login');
  };

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="navbar">
      <input placeholder="Tìm kiếm" className="navbar-search" />

      <div className="navbar-right" ref={dropdownRef}>
        <button className="navbar-button">Tạo mới</button>

        <div className="navbar-avatar" onClick={() => setOpen(!open)}>
          M
        </div>

        {open && (
          <div className="navbar-dropdown">
            <button onClick={handleLogout}>Logout</button>
          </div>
        )}
      </div>
    </div>
  );
}
